package com.example.nstreetview.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nsv_images")
data class StreetViewImage(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  val videoId: String,
  val imageName: String,
  val lon: Double,
  val lat: Double,
  val imageTime: Long
)