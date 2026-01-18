package com.example.nstreetview.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nstreetview.data.entity.WayRtreeEntity

@Dao
interface WayRtreeDao {

  @Query(
    """
    SELECT way_id FROM nsv_ways_rtree
    WHERE min_lat <= :maxLat AND max_lat >= :minLat
    AND min_lon <= :maxLng AND max_lon >= :minLng
    """
  )
  fun queryCandidateWays(minLat: Double, maxLat: Double,
                         minLng: Double, maxLng: Double): List<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(rtreeEntries: List<WayRtreeEntity>)
}