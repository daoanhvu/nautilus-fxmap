package com.example.nstreetview.data.model

import java.time.OffsetDateTime

data class LocationInfo(
  val eventMillis: Long?,
  val lat: Double?,
  val lon: Double?,
  val bearing: Float?,
  val speed: Float?
)
