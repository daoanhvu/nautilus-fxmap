package com.example.nstreetview.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nsv_way_geometry")
data class WayGeometryEntity(
  @PrimaryKey
  @ColumnInfo(name = "way_id")
  val wayId: Long,
  val geom: String
)