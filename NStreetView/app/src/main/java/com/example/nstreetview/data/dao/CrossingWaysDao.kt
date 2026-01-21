package com.example.nstreetview.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nstreetview.data.entity.CrossingWayEntity

@Dao
interface CrossingWaysDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun addCrossWays(crossingWayEntity: CrossingWayEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(crossingWays: List<CrossingWayEntity>)

  @Query("SELECT * FROM nsv_cross_ways WHERE wayId > 0")
  fun getAllCrossingWays(): List<CrossingWayEntity>

  @Query("SELECT * FROM nsv_cross_ways WHERE crossingId = :crossId")
  fun getAllWaysByCrossId(crossId: Long): List<CrossingWayEntity>
}