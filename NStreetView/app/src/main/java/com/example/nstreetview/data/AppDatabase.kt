package com.example.nstreetview.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nstreetview.data.dao.CrossingPointDao
import com.example.nstreetview.data.dao.CrossingWaysDao
import com.example.nstreetview.data.dao.StreetViewImageDao
import com.example.nstreetview.data.dao.StreetWayDao
import com.example.nstreetview.data.dao.WayGeometryDao
import com.example.nstreetview.data.dao.WayRtreeDao
import com.example.nstreetview.data.entity.CrossingPointEntity
import com.example.nstreetview.data.entity.CrossingWayEntity
import com.example.nstreetview.data.entity.StreetViewImage
import com.example.nstreetview.data.entity.WayEntity
import com.example.nstreetview.data.entity.WayGeometryEntity
import com.example.nstreetview.data.entity.WayRtreeEntity

@Database(entities = [StreetViewImage::class,
  WayEntity::class,
  WayRtreeEntity::class,
  WayGeometryEntity::class,
  CrossingPointEntity::class,
  CrossingWayEntity::class],
  version = 1,
  exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun streetViewImageDao(): StreetViewImageDao
  abstract fun streetWayDao(): StreetWayDao
  abstract fun wayRtreeDao(): WayRtreeDao
  abstract fun wayGeometryDao(): WayGeometryDao
  abstract fun crossingPointDao(): CrossingPointDao
  abstract fun crossingWaysDao(): CrossingWaysDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_1_2 = object: Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
              CREATE VIRTUAL TABLE IF NOT EXISTS `nsv_ways_rtree` USING rtree (
                way_id,
                min_lat, 
                max_lat,
                min_lon, 
                max_lon)
            """
        )
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "nsv_database"
        ).addCallback(object: Callback() {
          override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("""
              CREATE VIRTUAL TABLE IF NOT EXISTS `nsv_ways_rtree` USING rtree (
                way_id,
                min_lat, 
                max_lat,
                min_lon, 
                max_lon
              );
            """)
          }
        }).addMigrations(MIGRATION_1_2)
          .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
