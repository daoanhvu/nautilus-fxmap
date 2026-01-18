package com.example.nstreetview.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nsv_nodes")
data class NodePointEntity(
  @PrimaryKey
  val id: Long,
  val lat: Double,
  val lon: Double,
  val type: Int
)
