package com.akrippner.mon.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.Group
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * Mon with a fishing rod, built as an animated vector in the way of "Making Jellyfish move in
 * Compose": the logo is one path in a static group, the rod is a second group that rotates about
 * Mon's hand, and the hanging line is a path recomputed from the rotated rod tip on each frame.
 *
 * Units are dp, in a viewport of ANGLER_WIDTH × ANGLER_HEIGHT whose bottom edge is the edge Mon
 * stands on. The line ends at (ANGLER_LINE_END_X, ANGLER_HEIGHT), where a reel outline starts.
 */

/** The size of the angler's viewport, in dp. */
internal const val ANGLER_WIDTH = 44f

internal const val ANGLER_HEIGHT = 24f

/** Where the line meets the edge Mon stands on, from the viewport's left, in dp. */
internal const val ANGLER_LINE_END_X = 37f

private const val BODY_HEIGHT = 21f
private const val BODY_SCALE = BODY_HEIGHT / MON_LOGO_HEIGHT
private const val BODY_LEFT = 10f
private const val BODY_TOP = ANGLER_HEIGHT - BODY_HEIGHT

// Mon's right hand in logo units, where the rod is held.
private const val HAND_X = BODY_LEFT + 74f * BODY_SCALE
private const val HAND_Y = BODY_TOP + 76f * BODY_SCALE

// The rod tip at rest, in viewport units.
private const val TIP_X = 32f
private const val TIP_Y = 2f

/** The logo is line art: a stroke this wide (in logo units) keeps it legible at 21 dp. */
private const val BODY_STROKE = 7f

private val monLogoNodes by lazy { addPathNodes(MON_LOGO_PATH) }

/**
 * Paints Mon holding a rod, sized [ANGLER_WIDTH] × [ANGLER_HEIGHT] dp. While [reeling] the rod bobs
 * as if winding in; otherwise it is held still and slightly raised.
 */
@Composable
internal fun rememberMonAnglerPainter(reeling: Boolean, color: Color): VectorPainter {
  // Read only inside the vector's content below, so each frame recomposes the vector alone.
  val bob =
    if (reeling) {
      rememberInfiniteTransition(label = "rod")
        .animateFloat(
          initialValue = -5f,
          targetValue = 5f,
          animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
          label = "rod bob",
        )
    } else {
      null
    }
  val ink = SolidColor(color)

  return rememberVectorPainter(
    defaultWidth = ANGLER_WIDTH.dp,
    defaultHeight = ANGLER_HEIGHT.dp,
    viewportWidth = ANGLER_WIDTH,
    viewportHeight = ANGLER_HEIGHT,
    name = "MonAngler",
    autoMirror = false,
  ) { _, _ ->
    val rodAngle = bob?.value ?: -6f
    Group(
      name = "body",
      translationX = BODY_LEFT,
      translationY = BODY_TOP,
      scaleX = BODY_SCALE,
      scaleY = BODY_SCALE,
    ) {
      Path(
        pathData = monLogoNodes,
        pathFillType = PathFillType.EvenOdd,
        fill = ink,
        stroke = ink,
        strokeLineWidth = BODY_STROKE,
        strokeLineJoin = StrokeJoin.Round,
      )
    }
    Group(name = "rod", rotation = rodAngle, pivotX = HAND_X, pivotY = HAND_Y) {
      Path(
        pathData =
          PathData {
            moveTo(HAND_X - 1.5f, HAND_Y + 1.4f)
            lineTo(TIP_X, TIP_Y)
          },
        stroke = ink,
        strokeLineWidth = 1.3f,
        strokeLineCap = StrokeCap.Round,
      )
    }
    // The line hangs from wherever the rod tip is now, so it is outside the rotating group.
    val radians = rodAngle * PI.toFloat() / 180f
    val dx = TIP_X - HAND_X
    val dy = TIP_Y - HAND_Y
    val tipX = HAND_X + dx * cos(radians) - dy * sin(radians)
    val tipY = HAND_Y + dx * sin(radians) + dy * cos(radians)
    Path(
      pathData =
        PathData {
          moveTo(tipX, tipY)
          lineTo(ANGLER_LINE_END_X, ANGLER_HEIGHT)
        },
      stroke = ink,
      strokeLineWidth = 0.8f,
      strokeLineCap = StrokeCap.Round,
    )
  }
}
