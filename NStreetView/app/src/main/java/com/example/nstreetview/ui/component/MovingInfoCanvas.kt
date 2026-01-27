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
import com.example.nstreetview.ui.model.LEFT_AND_STRAIGHT
import com.example.nstreetview.ui.model.LaneInfo
import com.example.nstreetview.ui.model.LanePanel
import com.example.nstreetview.ui.model.RIGHT_AND_STRAIGHT
import com.example.nstreetview.ui.model.STRAIGHT


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
    val lane1 = LaneInfo(LEFT_AND_STRAIGHT, true, null)
    val lane2 = LaneInfo(STRAIGHT, false, null)
    val lane3 = LaneInfo(RIGHT_AND_STRAIGHT, false, null)
    val lanes = LanePanel(1L, listOf(lane1, lane2, lane3), 500f, 300f)
    drawLaneInfo(100f, 600f, lanes)
    drawLeftAndUTurn(200f, 800f, 300f, 300f, 120f, Color.Blue)
//    drawRightUTurn(Color.Blue, center.x, 400f, 100f)
//    drawLeftUTurn(Color.Blue, center.x, 700f, 100f)
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
  val panelLaneWidth = 120f
  val radius = 180f
  var drawX = x
  for (lane in lanes.lanes) {
//    if (lane.canTurnRight) {
//      drawTurnRightSign(drawX, y, Color.Blue, radius)
//    }
//    if (lane.canTurnLeft) {
//      drawTurnLeftSign(drawX, y, Color.Blue, radius)
//    }
//
//    if (lane.canGoAhead) {
//      drawGoStraight(drawX, y, 100f, Color.Blue)
//    }
//
//    if (lane.canUTurnLeft) {
//      drawLeftUTurn(Color.Blue, drawX, y, radius)
//    }
//
//    if (lane.canUTurnRight) {
//      drawRightUTurn(Color.Blue, drawX, y, radius)
//    }

    drawX += panelLaneWidth
  }
}

fun DrawScope.drawGoStraight(drawX: Float,
                             drawY: Float,
                             height: Float,
                             drawColor: Color) {
  val arrowHeadSize = height * 0.4f
  val strokeWidth = height * 0.3f
  drawLine(
    start = Offset(drawX, drawY + height),
    end = Offset(drawX, drawY + arrowHeadSize),
    color = drawColor,
    strokeWidth = strokeWidth
  )

  // Draw the arrowhead
  val arrowHeadPath = Path()
  val arrowBaseY = drawY + arrowHeadSize
  arrowHeadPath.moveTo(drawX, drawY)
  arrowHeadPath.lineTo(drawX - arrowHeadSize / 1.5f, arrowBaseY)
  arrowHeadPath.lineTo(drawX + arrowHeadSize / 1.5f, arrowBaseY)
  arrowHeadPath.close()

  drawPath(
    path = arrowHeadPath,
    color = drawColor,
    style = Fill
  )
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
  posX: Float,
  posY: Float,
  height: Float,
  arrowWidth: Float,
  radius: Float,
  arrowColor: Color) {

  val arrowPath = Path()
  val arrowWidth = radius * 0.2f
  val arrowHeight = radius * 0.5f
  val turnRadius = radius * 0.1f
  val arrowHeadSize = radius * 0.25f

  // Y-Coordinates for the vertical bars
  val haftArrowHeight = arrowHeight / 2
//  val leftBarBottom = posY
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
  arrowPath.lineTo(leftLegX, posY)

  // Draw the path with a thick stroke
  drawPath(
    path = arrowPath,
    color = arrowColor,
    style = Stroke(width = arrowWidth, cap = StrokeCap.Round)
  )

  // Draw the arrowhead
  val arrowHeadPath = Path()
  val arrowTipY = posY + arrowHeadSize * 0.7f

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

fun DrawScope.drawTurnLeftSign(
  drawX: Float,
  drawY: Float,
  arrowColor: Color,
  radius: Float
) {
  // 1. The Left Turn Arrow
  val arrowWidth = radius * 0.18f
  val turnRadius = radius * 0.25f
  val arrowPath = Path().apply {
    // Start at the bottom right area
    val startX = drawX + (radius * 0.3f)
    val startY = drawY + (radius * 0.4f)
    moveTo(startX, startY)
    // Line up
    lineTo(startX, drawY - (radius * 0.1f))
    // Arc Left
    arcTo(
      rect = Rect(startX - (turnRadius * 2), drawY - (radius * 0.1f) - turnRadius, startX, drawY - (radius * 0.1f) + turnRadius),
      startAngleDegrees = 0f,
      sweepAngleDegrees = -90f,
      forceMoveTo = false
    )
    // Line Left
    lineTo(drawX - (radius * 0.4f), drawY - (radius * 0.1f) - turnRadius)
  }

  drawPath(path = arrowPath, color = arrowColor, style = Stroke(width = arrowWidth, cap = StrokeCap.Butt))

  // 2. Left Arrowhead
  val headSize = radius * 0.25f
  val headPath = Path().apply {
    val tipX = drawX - (radius * 0.55f)
    val tipY = drawY - (radius * 0.1f) - turnRadius
    moveTo(tipX, tipY)
    lineTo(tipX + headSize, tipY - (headSize * 0.6f))
    lineTo(tipX + headSize, tipY + (headSize * 0.6f))
    close()
  }
  drawPath(path = headPath, color = arrowColor, style = Fill)
}

fun DrawScope.drawTurnRightSign(drawX: Float,
                                drawY: Float,
                                arrowColor: Color,
                                radius: Float) {
  val arrowWidth = radius * 0.18f
  val turnRadius = radius * 0.25f
  // Arrow: Starts bottom-left, goes up, turns right
  val arrowPath = Path().apply {
    val startX = drawX - (radius * 0.3f)
    val startY = drawY + (radius * 0.4f)
    moveTo(startX, startY)
    lineTo(startX, drawY - (radius * 0.1f))
    arcTo(
      rect = Rect(startX, drawY - (radius * 0.1f) - turnRadius, startX + (turnRadius * 2), drawY - (radius * 0.1f) + turnRadius),
      startAngleDegrees = 180f,
      sweepAngleDegrees = 90f,
      forceMoveTo = false
    )
    lineTo(drawX + (radius * 0.4f), drawY - (radius * 0.1f) - turnRadius)
  }
  drawPath(path = arrowPath, color = arrowColor, style = Stroke(width = arrowWidth, cap = StrokeCap.Butt))

  // Right Arrowhead
  val headSize = radius * 0.25f
  val headPath = Path().apply {
//    val tipX = drawX + (radius * 0.45f)
    val tipX = drawX + (radius * 0.55f)
    val tipY = drawY - (radius * 0.1f) - turnRadius
    moveTo(tipX, tipY)
    lineTo(tipX - headSize, tipY - (headSize * 0.6f))
    lineTo(tipX - headSize, tipY + (headSize * 0.6f))
    close()
  }
  drawPath(path = headPath, color = arrowColor, style = Fill)
}


/**
 * params:
 *  drawX: x of the left-top corner of the sign
 *  drawY: y of the left-top corner of the sign
 */
private fun DrawScope.drawLeftAndUTurn(
  drawX: Float,
  drawY: Float,
  signWidth: Float,
  signHeight: Float,
  radius: Float,
  arrowColor: Color) {

  val arrowPath = Path()
  val arrowWidth = radius * 0.6f
  // Distance from the tip of the arrow to the arrow's baseline
  val arrowHeadSize = signHeight * 0.25f

  // Start at the bottom of the left vertical line
  val haftWidth = arrowWidth / 2

  val arrowWingWidth = arrowHeadSize * 0.4f

  arrowPath.moveTo(drawX, drawY + arrowWingWidth + haftWidth)
  arrowPath.lineTo(drawX + arrowHeadSize, drawY)
  arrowPath.lineTo(drawX + arrowHeadSize, drawY + arrowWingWidth)
  arrowPath.lineTo(drawX + signWidth - arrowWidth, drawY + arrowWingWidth)

  arrowPath.arcTo(
    rect = Rect(
      left = drawX + signWidth - arrowWidth,
      top = drawY + arrowWingWidth,
      right = drawX + signWidth,
      bottom = drawY + arrowWingWidth + arrowWidth
    ),
    startAngleDegrees = 270f,
    sweepAngleDegrees = 90f,
    forceMoveTo = false
  )
  arrowPath.lineTo(drawX + signWidth, drawY + signHeight)
  arrowPath.lineTo(drawX + signWidth - arrowWidth, drawY + signHeight)
  arrowPath.lineTo(drawX + signWidth - arrowWidth, drawY + signHeight - arrowWidth)

  arrowPath.arcTo(
    rect = Rect(
      left = drawX + signWidth - arrowWidth - arrowWidth,
      top = drawY + signHeight - arrowWidth - arrowWidth,
      right = drawX + signWidth - arrowWidth,
      bottom = drawY + signHeight - arrowWidth
    ),
    startAngleDegrees = 0f,
    sweepAngleDegrees = -180f,
    forceMoveTo = false
  )
  arrowPath.lineTo(drawX + signWidth - arrowWidth - arrowWidth, drawY + signHeight)
  arrowPath.lineTo(drawX + signWidth - arrowWidth - arrowWingWidth, drawY + signHeight)

  arrowPath.lineTo((drawX + (drawX + signWidth - arrowWidth - arrowWingWidth)) / 2.0f, drawY + signHeight + arrowHeadSize)
  arrowPath.lineTo(drawX, drawY + signHeight)
  arrowPath.lineTo(drawX + arrowWingWidth, drawY + signHeight)

  drawPath(
    path = arrowPath,
    color = arrowColor,
    style = Stroke(width = 2f)
  )
//
//  // Draw the arrowhead
//  val arrowHeadPath = Path()
//  val arrowTipY = drawY + arrowHeadSize * 0.7f
//
//  arrowHeadPath.moveTo(leftLegX, arrowTipY)
//  arrowHeadPath.lineTo(leftLegX - arrowHeadSize / 1.5f, arrowTipY - arrowHeadSize)
//  arrowHeadPath.lineTo(leftLegX + arrowHeadSize / 1.5f, arrowTipY - arrowHeadSize)
//  arrowHeadPath.close()
//
//  drawPath(
//    path = arrowHeadPath,
//    color = arrowColor,
//    style = Fill
//  )
}