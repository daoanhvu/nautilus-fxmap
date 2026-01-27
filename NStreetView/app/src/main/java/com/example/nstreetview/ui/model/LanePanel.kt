package com.example.nstreetview.ui.model

data class LanePanel(
  val wayId: Long,
  val lanes: List<LaneInfo>,
  val drawingWidth: Float?,
  val drawingHeight: Float
)
