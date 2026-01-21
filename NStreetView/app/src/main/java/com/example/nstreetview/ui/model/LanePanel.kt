package com.example.nstreetview.ui.model

data class LanePanel(
  val wayId: Long,
  val lanes: List<LaneInfo>,
  val width: Float?
)
