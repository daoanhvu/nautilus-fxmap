package com.example.nstreetview.ui.model

data class LaneInfo(
  val canTurnLeft: Boolean,
  val canUTurnLeft: Boolean,
  val canTurnRight: Boolean,
  val canUTurnRight: Boolean,
  var canGoAhead: Boolean
)
