package com.example.nstreetview.data.model

data class CrossingPointWithDistance(
  val id: Long,
  val lat: Double,
  val lon: Double,
  val distance: Double,
  val signalType: String?
)
