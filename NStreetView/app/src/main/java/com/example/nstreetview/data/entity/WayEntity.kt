package com.example.nstreetview.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nsv_ways")
data class WayEntity (
  @PrimaryKey
  val id: Long,
  val highway: String?,
  val name: String?,
  val width: Double?,
  val oneway: Boolean?,
  val maxSpeed: Int?,
  val minSpeed: Int?,
  val lanes: Int?,
  val ref: String?
)