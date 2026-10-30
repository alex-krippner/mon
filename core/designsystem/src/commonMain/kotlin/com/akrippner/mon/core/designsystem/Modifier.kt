package com.akrippner.mon.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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

@Composable
inline fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier =
  if (condition) this.modifier() else this

@Composable
fun Modifier.bottomBorder(strokeWidth: Dp, color: Color): Modifier {
  val density = LocalDensity.current
  val strokeWidthPx = density.run { strokeWidth.toPx() }

  return this.then(
    Modifier.drawBehind {
      val width = size.width
      val height = size.height - strokeWidthPx / 2

      drawLine(
        color = color,
        start = Offset(x = 0f, y = height),
        end = Offset(x = width, y = height),
        strokeWidth = strokeWidthPx,
      )
    }
  )
}

@Composable
fun Modifier.topBorder(strokeWidth: Dp, color: Color): Modifier {
  val density = LocalDensity.current
  val strokeWidthPx = density.run { strokeWidth.toPx() }

  return this.then(
    Modifier.drawBehind {
      val width = size.width
      val y = strokeWidthPx / 2

      drawLine(
        color = color,
        start = Offset(x = 0f, y = y),
        end = Offset(x = width, y = y),
        strokeWidth = strokeWidthPx,
      )
    }
  )
}
