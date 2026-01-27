package com.example.nstreetview.ui.model

const val STRAIGHT         = 0b0000_0000_0000_0000_0000_0000_0000_0001
const val LEFT_TURN           = 0b0000_0000_0000_0000_0000_0000_0000_0010
const val RIGHT_TURN          = 0b0000_0000_0000_0000_0000_0000_0000_0100
const val LEFT_U_TURN         = 0b0000_0000_0000_0000_0000_0000_0000_0100
const val RIGHT_U_TURN        = 0b0000_0000_0000_0000_0000_0000_0000_1000
const val LEFT_AND_STRAIGHT   = 0b0000_0000_0000_0000_0000_0000_0000_0011
const val RIGHT_AND_STRAIGHT  = 0b0000_0000_0000_0000_0000_0000_0000_0101
const val LEFT_RIGHT_STRAIGHT = 0b0000_0000_0000_0000_0000_0000_0000_0111

data class LaneInfo(
  val direction: Int,
  val extended: Boolean,
  val width: Float?
)
