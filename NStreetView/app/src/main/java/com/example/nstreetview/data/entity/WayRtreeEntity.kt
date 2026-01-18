package com.example.nstreetview.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "nsv_ways_rtree")
data class WayRtreeEntity(
  @ColumnInfo(name = "way_id")
  val id: Long,
  @ColumnInfo(name = "min_lat")
  val minLat: Double,
  @ColumnInfo(name = "max_lat")
  val maxLat: Double,
  @ColumnInfo(name = "min_lon")
  val minLon: Double,
  @ColumnInfo(name = "max_lon")
  val maxLon: Double
)
