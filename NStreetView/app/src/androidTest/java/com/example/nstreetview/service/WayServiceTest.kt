import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.data.entity.CrossingPointEntity
import com.example.nstreetview.data.entity.WayEntity
import com.example.nstreetview.data.entity.WayGeometryEntity
import com.example.nstreetview.data.entity.WayRtreeEntity
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
    setupTestData()
  }

  private fun setupTestData() {
    runBlocking {
      // A straight road segment for map matching
      val way = WayEntity(
        id = 1,
        highway = "primary",
        name = "Main St",
        width = 8.0,
        oneway = false,
        maxSpeed = 60,
        minSpeed = 0,
        lanes = 2,
        ref = "A1"
      )
      val wayGeom =
        WayGeometryEntity(wayId = 1, geom = "LINESTRING (106.7000 10.8000, 106.7010 10.8000)")
      val wayRtree = WayRtreeEntity(
        id = 1,
        minLat = 10.8000,
        maxLat = 10.8000,
        minLon = 106.7000,
        maxLon = 106.7010
      )

      db.streetWayDao().insertAll(listOf(way))
      db.wayGeometryDao().insertAll(listOf(wayGeom))
      db.wayRtreeDao().insertAll(listOf(wayRtree))

      // A crossing point for warning detection
      val crossing =
        CrossingPointEntity(id = 101, lat = 10.8500, lon = 106.7500, signalType = "traffic_signals")
      db.crossingPointDao().insertAll(listOf(crossing))
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
    val lat = 10.80001
    val lng = 106.7005
    // Vehicle is moving east, aligned with the road
    val prevLat = 10.80001
    val prevLng = 106.7004

    val result = wayService.match(lat, lng, prevLat, prevLng)

    assertNotNull("Map match should find a result", result)
    assertEquals("Should match with way ID 1", 1L, result?.wayId)
    assertTrue("Bearing should be around 90 degrees (East)", result!!.segmentBearingDeg in 89.0..91.0)
    assertTrue("Should detect forward movement", result.onForward)
  }

  @Test
  fun match_returnsNullWhenTooFarFromAnyWay() = runBlocking {
    // Point is far from any roads in the test DB
    val lat = 11.0
    val lng = 107.0

    val result = wayService.match(lat, lng, null, null)

    assertNull("Map match should return null for a point far from any way", result)
  }

  @Test
  fun detectCrossingWarning_findsCrossingAhead() = runBlocking {
    // Car is south of the crossing, heading North (0 degrees)
    val carLat = 10.8490
    val carLon = 106.7500
    val heading = 0.0 // North

    val result = wayService.detectCrossingWarning(carLat, carLon, heading)

    assertNotNull("Should detect a crossing point ahead", result)
    assertEquals("Should be the crossing with ID 101", 101L, result?.id)
    assertEquals("traffic_signals", result?.signalType)
  }

  @Test
  fun detectCrossingWarning_returnsNullForCrossingBehind() = runBlocking {
    // Car is north of the crossing, heading North (0 degrees) - crossing is behind
    val carLat = 10.8510
    val carLon = 106.7500
    val heading = 0.0 // North

    val result = wayService.detectCrossingWarning(carLat, carLon, heading)

    assertNull("Should not detect a crossing point that is behind", result)
  }

  @Test
  fun detectCrossingWarning_returnsNullWhenTooFar() = runBlocking {
    // Car is 1km away from the crossing, heading towards it
    val carLat = 10.8400
    val carLon = 106.7500
    val heading = 0.0 // North

    val result = wayService.detectCrossingWarning(carLat, carLon, heading)

    assertNull("Should not detect a crossing point that is more than 400m away", result)
  }
}