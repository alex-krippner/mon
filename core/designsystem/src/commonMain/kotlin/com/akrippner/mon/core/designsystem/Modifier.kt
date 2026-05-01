package com.akrippner.mon.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

fun Modifier.dashedBorder(
  color: Color,
  strokeWidth: Dp,
  dashedLength: Dp,
  gapLength: Dp,
  cornerRadius: Dp,
): Modifier = drawBehind {
  val stroke = strokeWidth.toPx()
  val dash = dashedLength.toPx()
  val gap = gapLength.toPx()
  val r = cornerRadius.toPx()
  drawRoundRect(
    color = color,
    size = size,
    cornerRadius = CornerRadius(r),
    style = Stroke(width = stroke, pathEffect = dashPathEffect(floatArrayOf(dash, gap))),
  )
}
