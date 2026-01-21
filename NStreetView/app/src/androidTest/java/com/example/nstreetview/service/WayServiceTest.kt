import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nstreetview.R
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.data.entity.CrossingPointEntity
import com.example.nstreetview.data.entity.WayEntity
import com.example.nstreetview.data.entity.WayGeometryEntity
import com.example.nstreetview.data.entity.WayRtreeEntity
import com.example.nstreetview.repository.OsmRepository
import com.example.nstreetview.service.WayService
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * https://www.sunearthtools.com/tools/distance.php
 */
@RunWith(AndroidJUnit4::class)
class WayServiceTest {

  private lateinit var db: AppDatabase
  private lateinit var wayService: WayService

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .addCallback(object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
          super.onCreate(db)
          // Manually create the R-Tree virtual table for the in-memory DB
          db.execSQL(
            "CREATE VIRTUAL TABLE IF NOT EXISTS nsv_ways_rtree USING rtree(id, minLat, maxLat, minLon, maxLon);"
          )
        }
      })
      .build()

    wayService = WayService(db)
    setupTestData(context, db)
  }

  private fun setupTestData(context: Context, db: AppDatabase) {
    val osmRepository = OsmRepository(context, db)
    val resourceId = R.raw.pham_van_dong_kha_van_can_test
    // Run the repository method to import the data.
    runBlocking {
      osmRepository.importOsmDataFromResource(resourceId, context)
    }

  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    db.close()
  }

  @Test
  fun haversine_calculatesDistanceCorrectly() {
    val distance = wayService.haversine(10.8000, 106.7000, 10.8000, 106.7010)
    // Roughly 110 meters for 0.001 degrees of longitude at this latitude
    assertEquals(109.3, distance, 0.1)
  }

  @Test
  fun isAhead_returnsTrueForPointInFront() {
    // Car is at (10, 106), heading North (0 degrees)
    // Point is at (10.001, 106), directly ahead
    val isAhead = wayService.isAhead(10.0, 106.0, 0.0, 10.001, 106.0)
    assertTrue("Point directly ahead should be detected", isAhead)
  }

  @Test
  fun isAhead_returnsFalseForPointBehind() {
    // Car is at (10, 106), heading North (0 degrees)
    // Point is at (9.999, 106), directly behind
    val isAhead = wayService.isAhead(10.0, 106.0, 0.0, 9.999, 106.0)
    assertFalse("Point directly behind should not be detected", isAhead)
  }

  @Test
  fun isAhead_returnsFalseForPointToBehindRight() {
    // Car at (10, 106), heading North (0 degrees). Point is at 135 degrees (SE)
    val isAhead = wayService.isAhead(10.0, 106.0, 0.0, 9.999, 106.001)
    assertFalse("Point behind-right should not be detected", isAhead)
  }

  @Test
  fun match_findsCorrectWayAndBearing() = runBlocking {
    // Point is slightly off the center of our test road
    // 10.866458, 106.761738
    val lat = 10.866458
    val lng = 106.761738
    // Vehicle is moving east, aligned with the road
    //10.866037, 106.761197
    val prevLat = 10.866037
    val prevLng = 106.761197

    val result = wayService.match(lat, lng, prevLat, prevLng)
    assertNotNull("Map match should find a result", result)
    assertEquals("Should match with way ID 1217247991", 1217247991L, result?.wayId)
    assertEquals(49.7, result!!.segmentBearingDeg, 0.1)
    assertTrue("Should detect forward movement", result.onForward)
  }

  @Test
  fun match_returnsNullWhenTooFarFromAnyWay() = runBlocking {
    // Point is far from any roads in the test DB
    // 10.866503, 106.760926
    val lat = 10.866503
    val lng = 106.760926
    val result = wayService.match(lat, lng, null, null)
    assertNull("Map match should return null for a point far from any way", result)
  }

  @Test
  fun detectCrossingWarning_findsCrossingAhead() = runBlocking {
    // Car is south of the crossing, heading North (0 degrees)
    val carLat = 10.867467
    val carLon = 106.762398
    val carLat2 = 10.867156
    val carLon2 = 106.762192
    val heading = wayService.geoBearing(carLat, carLon, carLat2, carLon2)
    assertEquals(213.04, heading, 0.1)

    val wayMatchingResult = wayService.match(carLat, carLon, carLat2, carLon2)
    assertNotNull("Should detect a way", wayMatchingResult)
    assertEquals("Phạm Văn Đồng", wayMatchingResult!!.name)
    val result = wayService.detectCrossingWarning(carLat2, carLon2, heading, wayMatchingResult.wayId)
    assertNotNull("Should detect a crossing point ahead", result)
    assertEquals("Should be the crossing with ID 9207017673", 9207017673L, result?.id)
    assertEquals("traffic_signals", result?.signalType)
  }

  @Test
  fun detectCrossingWarning_returnsNullForCrossingBehind() = runBlocking {
    // Car is north of the crossing, heading North (0 degrees) - crossing is behind
    val carLat = 10.8510
    val carLon = 106.7500
    val heading = 0.0 // North

    val result = wayService.detectCrossingWarning(carLat, carLon, heading, 1)
    assertNull("Should not detect a crossing point that is behind", result)
  }

  @Test
  fun detectCrossingWarning_returnsNullWhenTooFar() = runBlocking {
    // Car is 1km away from the crossing, heading towards it
    val carLat = 10.8400
    val carLon = 106.7500
    val heading = 0.0 // North

    val result = wayService.detectCrossingWarning(carLat, carLon, heading, 1)
    assertNull("Should not detect a crossing point that is more than 400m away", result)
  }
}