package com.example.nstreetview.data.model

import java.util.Queue

data class VehicleInfo(
  val number: String?,
  val manufacturer: String?,
  val model: String?,
  val owner: String?,
  val vehicleType: Int?,
  val locations: Queue<LocationInfo>
)
