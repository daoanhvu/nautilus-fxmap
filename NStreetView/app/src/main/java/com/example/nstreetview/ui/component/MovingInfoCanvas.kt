package com.example.nstreetview.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.nstreetview.ui.model.LanePanel


@Composable
fun MovingInfoCanvas(speedLimit: Int,
                     currentSpeed: Int,
                     x: Float,          // X position of the Speed Limit sign
                     y: Float,          // Y position of the Speed Limit sign
                     dx: Float,         // Horizontal offset for current speed
                     radius: Float = 100f) {
  val textMeasurer = rememberTextMeasurer()

  // Vertical offset calculation: 1.5x radius puts it below with a small gap
  val dy = radius * 1.5f

  Canvas(modifier = Modifier.fillMaxSize()) {
    // --- 1. DRAW SPEED LIMIT SIGN (Red Circle) ---
    val limitCenter = Offset(x, y)
    drawSpeedLimitSign(speedLimit, textMeasurer, radius, limitCenter)

    // --- 2. DRAW CURRENT SPEED SIGN (White Circle) ---
    // Positioned relative to the first sign
    val currentCenter = Offset(x + dx, y + dy)
    val currentSpeedRadius = radius * 0.6F // Slightly smaller than the limit sign
    drawCurrentSpeed(currentSpeed, speedLimit, currentSpeedRadius, textMeasurer, currentCenter)



    // Testing
    drawRightUTurn(Color.Blue, center.x, 400f, 100f)
    drawLeftUTurn(Color.Blue, center.x, 700f, 100f)
  }
}

private fun DrawScope.drawSpeedLimitSign(speedLimit: Int,
                                         textMeasurer: TextMeasurer,
                                         radius: Float, limitCenter: Offset) {
  // Outer Red
  drawCircle(color = Color.Red, radius = radius, center = limitCenter, style = Fill)
  // Inner White
  drawCircle(color = Color.White, radius = radius * 0.8f, center = limitCenter, style = Fill)

  // Speed Limit Text
  val limitTextLayout = textMeasurer.measure(
    text = speedLimit.toString(),
    style = TextStyle(fontSize = (radius * 0.4f).sp, fontWeight = FontWeight.Bold)
  )
  drawText(
    textLayoutResult = limitTextLayout, topLeft = Offset(
      limitCenter.x - (limitTextLayout.size.width / 2),
      limitCenter.y - (limitTextLayout.size.height / 2)
    )
  )
}

private fun DrawScope.drawCurrentSpeed(currentSpeed: Int, speedLimit: Int, radius: Float,
                                       textMeasurer: TextMeasurer,
                                       currentCenter: Offset) {
  // White background circle
  drawCircle(
    color = Color.White,
    radius = radius,
    center = currentCenter,
    style = Fill
  )
  // Thin gray border so it's visible on white backgrounds
  drawCircle(
    color = Color.Blue,
    radius = radius,
    center = currentCenter,
    style = Stroke(width = 4f)
  )
  // Current Speed Text
  val currentTextLayout = textMeasurer.measure(
    text = currentSpeed.toString(),
    style = TextStyle(
      color = if (currentSpeed > speedLimit) Color.Red else Color.Blue,
      fontSize = (radius * 0.5f).sp,
      fontWeight = FontWeight.ExtraBold
    )
  )
  drawText(
    textLayoutResult = currentTextLayout,
    topLeft = Offset(
      currentCenter.x - (currentTextLayout.size.width / 2),
      currentCenter.y - (currentTextLayout.size.height / 2)
    )
  )
}

private fun DrawScope.drawLaneInfo(
  x: Float,
  y: Float,
  lanes: LanePanel
) {
  val panelLaneWidth = 80f
  for (lane in lanes.lanes) {

  }
}

private fun DrawScope.drawRightUTurn(
  arrowColor: Color,
  posX: Float,
  posY: Float,
  radius: Float) {

  val arrowPath = Path()
  val arrowWidth = radius * 0.2f
  val arrowHeight = radius * 0.5f
  val turnRadius = radius * 0.1f
  val arrowHeadSize = radius * 0.25f

  // Y-Coordinates for the vertical bars
  val leftBarBottom = posY + (arrowHeight / 2)
  val rightBarBottom = posY // + (arrowHeight * 0.1f) // SHORTER: Changed from arrowHeight / 2
  val topOfBars = posY - (arrowHeight / 2)

  // Start at the bottom of the left vertical line
  val startX = posX - turnRadius - arrowWidth / 2

  arrowPath.moveTo(startX, leftBarBottom)
  // Line up to the start of the turn
  arrowPath.lineTo(startX, topOfBars)
  // Arc for the turn
  arrowPath.arcTo(
    rect = Rect(
      left = startX,
      top = topOfBars - turnRadius,
      right = startX + 2 * turnRadius + arrowWidth,
      bottom = topOfBars + turnRadius
    ),
    startAngleDegrees = 180f,
    sweepAngleDegrees = 180f,
    forceMoveTo = false
  )
  // Line down for the right side - Stopping EARLY
  val rightX = startX + 2 * turnRadius + arrowWidth
  arrowPath.lineTo(rightX, rightBarBottom)

  // Draw the path with a thick stroke
  drawPath(
    path = arrowPath,
    color = arrowColor,
    style = Stroke(width = arrowWidth, cap = StrokeCap.Round)
  )

  // Draw the arrowhead
  val arrowHeadPath = Path()
  val arrowTipY = rightBarBottom + arrowHeadSize * 0.7f

  arrowHeadPath.moveTo(rightX, arrowTipY)
  arrowHeadPath.lineTo(rightX - arrowHeadSize / 1.5f, arrowTipY - arrowHeadSize)
  arrowHeadPath.lineTo(rightX + arrowHeadSize / 1.5f, arrowTipY - arrowHeadSize)
  arrowHeadPath.close()

  drawPath(
    path = arrowHeadPath,
    color = arrowColor,
    style = Fill
  )
}

private fun DrawScope.drawLeftUTurn(
  arrowColor: Color,
  posX: Float,
  posY: Float,
  radius: Float) {

  val arrowPath = Path()
  val arrowWidth = radius * 0.2f
  val arrowHeight = radius * 0.5f
  val turnRadius = radius * 0.1f
  val arrowHeadSize = radius * 0.25f

  // Y-Coordinates for the vertical bars
  val haftArrowHeight = arrowHeight / 2
  val leftBarBottom = posY
  // The right leg is shorter one
  val rightBarBottom = posY + haftArrowHeight
  val topOfBars = posY - haftArrowHeight

  // Start at the bottom of the left vertical line
  val haftWidth = arrowWidth / 2
  val leftLegX = posX - turnRadius - haftWidth
  val rightLegX = posX + turnRadius + haftWidth

  arrowPath.moveTo(rightLegX, rightBarBottom)
  // Line up to the start of the turn
  arrowPath.lineTo(rightLegX, topOfBars)
  // Arc for the turn
  arrowPath.arcTo(
    rect = Rect(
      left = leftLegX,
      top = topOfBars - turnRadius,
      right = rightLegX,
      bottom = topOfBars + turnRadius
    ),
    startAngleDegrees = 0f,
    sweepAngleDegrees = -180f,
    forceMoveTo = false
  )
  // Line down for the right side
  arrowPath.lineTo(leftLegX, leftBarBottom)

  // Draw the path with a thick stroke
  drawPath(
    path = arrowPath,
    color = arrowColor,
    style = Stroke(width = arrowWidth, cap = StrokeCap.Round)
  )

  // Draw the arrowhead
  val arrowHeadPath = Path()
  val arrowTipY = leftBarBottom + arrowHeadSize * 0.7f

  arrowHeadPath.moveTo(leftLegX, arrowTipY)
  arrowHeadPath.lineTo(leftLegX - arrowHeadSize / 1.5f, arrowTipY - arrowHeadSize)
  arrowHeadPath.lineTo(leftLegX + arrowHeadSize / 1.5f, arrowTipY - arrowHeadSize)
  arrowHeadPath.close()

  drawPath(
    path = arrowHeadPath,
    color = arrowColor,
    style = Fill
  )
}

