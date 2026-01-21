package com.example.nstreetview.data.model

data class MapMatchResult(val wayId: Long,
                          val name: String?,
                          val distanceMeters: Double,
                          val segmentBearingDeg: Double,
                          val onForward: Boolean)