package com.example.nstreetview.data.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
  tableName = "nsv_cross_ways",
  primaryKeys = ["crossingId", "wayId"],
  indices = [Index("crossingId"), Index("wayId")]
)
data class CrossingWayEntity(
  val crossingId: Long,
  val wayId: Long
)
