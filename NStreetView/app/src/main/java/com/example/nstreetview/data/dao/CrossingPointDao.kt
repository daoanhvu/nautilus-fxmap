package com.example.nstreetview.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nstreetview.data.entity.CrossingPointEntity
import com.example.nstreetview.data.entity.CrossingWayEntity

@Dao
interface CrossingPointDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun addCrossPoint(cp: CrossingPointEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(crosses: List<CrossingPointEntity>)

  @Query("SELECT * FROM nsv_cross_points WHERE id = :crossPointId")
  fun getCrossById(crossPointId: Long): CrossingPointEntity?

  @Query("SELECT * FROM nsv_cross_points WHERE id > 0")
  fun getAllCrosses(): List<CrossingPointEntity>

  @Query("""
  SELECT * FROM nsv_cross_points
  WHERE lat BETWEEN :minLat AND :maxLat AND lon BETWEEN :minLon AND :maxLon
  """)
  suspend fun findNearby(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): List<CrossingPointEntity>

  @Query("""
  SELECT CP.* FROM nsv_cross_points AS CP
  INNER JOIN nsv_cross_ways as CW on CP.id = CW.crossingId
  WHERE ( CP.lat BETWEEN :minLat AND :maxLat AND CP.lon BETWEEN :minLon AND :maxLon )
  AND CW.wayId = :wayId
  """)
  suspend fun findNearbyByWayId(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    wayId: Long
  ): List<CrossingPointEntity>
}