package com.example.nstreetview.repository

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nstreetview.R
import com.example.nstreetview.data.AppDatabase
import com.example.nstreetview.data.dao.CrossingPointDao
import com.example.nstreetview.data.dao.CrossingWaysDao
import com.example.nstreetview.data.dao.StreetWayDao
import com.example.nstreetview.data.dao.WayGeometryDao
import com.example.nstreetview.data.dao.WayRtreeDao
import com.example.nstreetview.data.entity.WayRtreeEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test for OsmRepository, which tests the interaction
 * between the OsmParser and the AppDatabase.
 */
@RunWith(AndroidJUnit4::class)
class OsmRepositoryTest {

  private lateinit var inMemoryDatabase: AppDatabase
  private lateinit var streetWayDao: StreetWayDao
  private lateinit var wayGeometryDao: WayGeometryDao
  private lateinit var wayRtreeDao: WayRtreeDao
  private lateinit var crossingPointDao: CrossingPointDao
  private lateinit var crossingWayDao: CrossingWaysDao
  private lateinit var osmRepository: OsmRepository
  private lateinit var context: Context

  @Before
  fun createDb() {
    context = ApplicationProvider.getApplicationContext<Context>()
    // 1. Use an in-memory database for testing so it doesn't affect the real app data.
    inMemoryDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      // Allowing main thread queries, just for testing.
      .allowMainThreadQueries()
      // v-- This is the line to fix --v
      .addCallback(object : RoomDatabase.Callback() { // Change from AppDatabase.Callback
        // Manually create the virtual table for the in-memory DB
        override fun onCreate(db: SupportSQLiteDatabase) {
          super.onCreate(db)
          db.execSQL(
            """
              CREATE VIRTUAL TABLE IF NOT EXISTS nsv_ways_rtree USING rtree(id, minLat, maxLat, minLon, maxLon);
            """
          )
        }
      }).build()

    streetWayDao = inMemoryDatabase.streetWayDao()
    wayGeometryDao = inMemoryDatabase.wayGeometryDao()
    wayRtreeDao = inMemoryDatabase.wayRtreeDao()
    crossingPointDao = inMemoryDatabase.crossingPointDao()
    crossingWayDao = inMemoryDatabase.crossingWaysDao()

    // 2. Instantiate the repository, but with the test database instance
    // This is an important correction to ensure your test uses the in-memory DB
    osmRepository = OsmRepository(context, inMemoryDatabase)
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    // 3. Close the database after each test
    inMemoryDatabase.close()
  }

  @Test
  @Throws(Exception::class)
  fun importOsmDataFromResource_parsesAndInsertsDataIntoDatabase() = runBlocking {
    // Arrange
    // We will use the 'pham_van_dong_kha_van_can_test' file from the previous example.
    // Make sure it exists in 'app/src/main/res/raw/'.
    val resourceId = R.raw.pham_van_dong_kha_van_can_test

    // Act
    // Run the repository method to import the data.
    osmRepository.importOsmDataFromResource(resourceId, context)

    // Assert
    // Verify that the data was correctly inserted into each table.

    // 1. Check WayEntity table
    val allWays = streetWayDao.getAllWays() // Using Flow.first() to get current value
    assertEquals(24, allWays.size)
    val way685351780 = allWays.find { it.id == 685351780L }
    assertNotNull(way685351780)
    assertEquals("Phạm Văn Đồng", way685351780?.name)
    assertEquals("trunk", way685351780?.highway)

    // 2. Check WayGeometryEntity table
    val allGeometries = wayGeometryDao.getAllGeometries()
    assertEquals(24, allGeometries.size)
    val geom685351780 = wayGeometryDao.getGeometry(685351780L)
    assertNotNull(geom685351780)
    assertEquals("LINESTRING (106.7623961 10.8672291, 106.7625101 10.8674557, 106.7626368 10.86766, 106.7629279 10.8681163, 106.763075 10.8683703, 106.7631012 10.8684191, 106.7632109 10.8686234, 106.7633289 10.868876, 106.7633814 10.8690242, 106.763421 10.8691588, 106.7634601 10.8693351, 106.7634721 10.8694363, 106.7634944 10.8697752, 106.7635051 10.8702869, 106.7635062 10.8703183, 106.763521 10.8705184, 106.7635409 10.8706174, 106.7635519 10.8706725, 106.7635851 10.8707799, 106.7636529 10.8709504, 106.7637364 10.8711201, 106.7638294 10.8712848, 106.7639021 10.8713993, 106.7640355 10.8715774, 106.7641979 10.8717808)", geom685351780?.geom)

    // 3. Check CrossPointEntity table
    val allCrosses = crossingPointDao.getAllCrosses()
    assertEquals(10, allCrosses.size)
    val cross9207017673 = crossingPointDao.getCrossById(9207017673L)
    assertNotNull(cross9207017673)
    assertEquals("traffic_signals", cross9207017673?.signalType)

    // 4. Check CrossingWayEntity table
    val allCrossingWays = crossingWayDao.getAllCrossingWays()
    assertNotNull(allCrossingWays)
    assertEquals(10, allCrossingWays.size)

    //crossing: 9207017673
    //way: 1015442474,
    val crossingWays9207017673 = crossingWayDao.getAllWaysByCrossId(9207017673)
    assertNotNull(allCrossingWays)
    assertEquals(1, crossingWays9207017673.size)
    assertEquals(1015442474, crossingWays9207017673[0].wayId)

    // 5. Check WayRtreeEntity table (Virtual Table)
    // A simple way to test the R-tree is to perform a query that should find our item.
    val searchBoundingBox = WayRtreeEntity(
      0, 10.8659825, 10.8663887, 106.7616452, 106.7621076)
    val foundWays = wayRtreeDao.queryCandidateWays(
      searchBoundingBox.minLat,
      searchBoundingBox.maxLat,
      searchBoundingBox.minLon,
      searchBoundingBox.maxLon
    )
    assertEquals("Should find 10 ways within the bounding box", 10, foundWays.size)
    assertNotNull(foundWays.first { it == 328180407L })
  }
}