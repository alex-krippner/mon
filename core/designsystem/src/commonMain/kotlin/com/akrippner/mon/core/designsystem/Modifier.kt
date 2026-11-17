package com.akrippner.mon.core.designsystem

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

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

/**
 * Hover, pressed and keyboard-focus feedback in the Mon style: an ink overlay (8 % hovered, 16 %
 * pressed) over the content, and a 2 dp ink ring 2 dp outside [shape] while focused. Put it before
 * any `clip` in the chain, or the ring is clipped away, and pass the same [interactionSource] to
 * the `clickable` with `indication = null`.
 */
@Composable
fun Modifier.stateLayer(interactionSource: InteractionSource, shape: Shape): Modifier {
  val hovered by interactionSource.collectIsHoveredAsState()
  val pressed by interactionSource.collectIsPressedAsState()
  val focused by interactionSource.collectIsFocusedAsState()
  val ink = MonTheme.colors.textPrimary
  val overlayAlpha =
    when {
      pressed -> 0.16f
      hovered -> 0.08f
      else -> 0f
    }
  return drawWithContent {
    drawContent()
    if (overlayAlpha > 0f) {
      drawOutline(shape.createOutline(size, layoutDirection, this), ink.copy(alpha = overlayAlpha))
    }
    if (focused) {
      val ring = 2.dp.toPx()
      val grow = 2.dp.toPx() + ring / 2
      val ringSize = Size(size.width + grow * 2, size.height + grow * 2)
      translate(-grow, -grow) {
        drawOutline(
          shape.createOutline(ringSize, layoutDirection, this),
          ink,
          style = Stroke(width = ring),
        )
      }
    }
  }
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
