package com.example.nstreetview.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "nsv_cross_points", indices = [Index("lat"), Index("lon")])
data class CrossingPointEntity(
  @PrimaryKey val id: Long,
  val lat: Double,
  val lon: Double,
  val signalType: String?
)
