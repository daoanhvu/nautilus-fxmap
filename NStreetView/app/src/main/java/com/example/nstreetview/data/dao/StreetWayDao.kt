package com.example.nstreetview.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nstreetview.data.entity.WayEntity

@Dao
interface StreetWayDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun addWay(way: WayEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(ways: List<WayEntity>)

  @Query("SELECT width FROM nsv_ways WHERE id = :wayId")
  fun getWidth(wayId: Long): Float?

  @Query("SELECT * FROM nsv_ways WHERE id = :wayId")
  fun getWayById(wayId: Long): WayEntity?

  @Query("SELECT * FROM nsv_ways WHERE id > 0")
  fun getAllWays(): List<WayEntity>
}