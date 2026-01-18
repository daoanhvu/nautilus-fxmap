package com.example.nstreetview.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nstreetview.data.entity.WayGeometryEntity

@Dao
interface WayGeometryDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun addGeometry(aGeometry: WayGeometryEntity)

  @Query("SELECT geom FROM nsv_way_geometry WHERE way_id = :wayId")
  fun getGeometryText(wayId: Long): String?

  @Query("SELECT * FROM nsv_way_geometry WHERE way_id = :wayId")
  fun getGeometry(wayId: Long): WayGeometryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(geometries: List<WayGeometryEntity>)

  @Query("SELECT * FROM nsv_way_geometry WHERE way_id > 0")
  fun getAllGeometries(): List<WayGeometryEntity>
}