package com.akrippner.mon.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import kotlin.math.PI
import kotlin.math.atan2

/** Where a [reelProgress] download stands. */
enum class ReelPhase {
  /** Nothing is downloading: nothing is drawn (a previous reel fades out). */
  Idle,
  /** Mon reels the pack in along the outline as progress grows. */
  Reeling,
  /** The download is done and the pack sits next to Mon while it is unpacked. */
  Landed,
}

/**
 * The download animation of a tile: Mon stands on the tile's top edge with a rod, and a dashed
 * fishing line runs from the rod around the tile's outline to a small book. As [progress] (0 to 1)
 * grows, the line shortens and the book is reeled in, reaching Mon at 1.
 *
 * Mon is drawn [ANGLER_HEIGHT] dp above the tile, so leave that much room above it. Put this
 * modifier before any `clip`, or the line and Mon are clipped away. [cornerRadius] must match the
 * tile's shape.
 */
@Composable
fun Modifier.reelProgress(progress: Float, phase: ReelPhase, cornerRadius: Dp = 12.dp): Modifier {
  val ink = MonTheme.colors.textPrimary
  val bookFill = MonTheme.colors.surfaceRaised
  val shownState =
    animateFloatAsState(
      targetValue = if (phase == ReelPhase.Idle) 0f else 1f,
      animationSpec = tween(durationMillis = 450),
      label = "reel visibility",
    )
  // Progress arrives in whole percents; easing between them keeps the book gliding.
  val reeled by
    animateFloatAsState(
      targetValue = if (phase == ReelPhase.Landed) 1f else progress.coerceIn(0f, 1f),
      animationSpec = tween(durationMillis = 400),
      label = "reel progress",
    )
  // An idle tile runs no animation at all once a previous reel has faded out.
  if (phase == ReelPhase.Idle && shownState.value == 0f) return this
  val shown by shownState
  val wiggle =
    if (phase == ReelPhase.Landed) {
      rememberInfiniteTransition(label = "landed book")
        .animateFloat(
          initialValue = -8f,
          targetValue = 8f,
          animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
          label = "book wiggle",
        )
    } else {
      null
    }
  val angler = rememberMonAnglerPainter(reeling = phase == ReelPhase.Reeling, color = ink)
  val measure = remember { PathMeasure() }
  val segment = remember { Path() }

  return drawWithCache {
    val radius = cornerRadius.toPx()
    val outline = tileOutline(size, radius, startX = ANGLER_LINE_END_X.dp.toPx())
    measure.setPath(outline, forceClosed = false)
    val perimeter = measure.length
    val line =
      Stroke(
        width = 2.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.5.dp.toPx(), 3.5.dp.toPx())),
      )
    val anglerSize = Size(ANGLER_WIDTH.dp.toPx(), ANGLER_HEIGHT.dp.toPx())

    onDrawWithContent {
      drawContent()
      if (shown <= 0f) return@onDrawWithContent
      // At 0 % the line stops just short of closing the loop, so the book is not on Mon's feet.
      val length = perimeter * 0.97f * (1f - reeled)
      if (length > 0.5f) {
        segment.reset()
        measure.getSegment(0f, length, segment, startWithMoveTo = true)
        drawPath(segment, ink, alpha = shown, style = line)
      }
      val tangent = measure.getTangent(length)
      val angle = uprightAngle(atan2(tangent.y, tangent.x) * 180f / PI.toFloat())
      drawBook(
        center = measure.getPosition(length),
        degrees = angle + (wiggle?.value ?: 0f),
        ink = ink,
        fill = bookFill,
        alpha = shown,
      )
      translate(top = -anglerSize.height) { with(angler) { draw(anglerSize, alpha = shown) } }
    }
  }
}

/** The tile's outline, clockwise, starting [startX] in from the left on the top edge. */
private fun tileOutline(size: Size, radius: Float, startX: Float): Path {
  val w = size.width
  val h = size.height
  val d = radius * 2
  return Path().apply {
    moveTo(startX, 0f)
    lineTo(w - radius, 0f)
    arcTo(Rect(w - d, 0f, w, d), -90f, 90f, forceMoveTo = false)
    lineTo(w, h - radius)
    arcTo(Rect(w - d, h - d, w, h), 0f, 90f, forceMoveTo = false)
    lineTo(radius, h)
    arcTo(Rect(0f, h - d, d, h), 90f, 90f, forceMoveTo = false)
    lineTo(0f, radius)
    arcTo(Rect(0f, 0f, d, d), 180f, 90f, forceMoveTo = false)
    lineTo(startX, 0f)
  }
}

/** Folds a direction into 0 to 90 degrees, so the book leans with the outline but never flips. */
private fun uprightAngle(degrees: Float): Float {
  val t = ((degrees % 360f) + 360f) % 360f
  return when {
    t <= 90f -> t
    t <= 180f -> 180f - t
    t <= 270f -> t - 180f
    else -> 360f - t
  }
}

/** A 16 × 20 dp book: a paper cover with an ink spine and three lines of text. */
private fun DrawScope.drawBook(
  center: Offset,
  degrees: Float,
  ink: Color,
  fill: Color,
  alpha: Float,
) {
  val w = 14.dp.toPx()
  val h = 18.dp.toPx()
  val topLeft = Offset(center.x - w / 2, center.y - h / 2)
  rotate(degrees, pivot = center) {
    drawRoundRect(fill, topLeft, Size(w, h), CornerRadius(3.dp.toPx()), alpha = alpha)
    drawRoundRect(
      ink,
      topLeft,
      Size(w, h),
      CornerRadius(3.dp.toPx()),
      alpha = alpha,
      style = Stroke(width = 2.dp.toPx()),
    )
    val thin = 1.5.dp.toPx()
    val spineX = topLeft.x + 4.dp.toPx()
    drawLine(
      ink,
      Offset(spineX, topLeft.y + 0.5.dp.toPx()),
      Offset(spineX, topLeft.y + h - 0.5.dp.toPx()),
      thin,
      StrokeCap.Round,
      alpha = alpha,
    )
    for (row in 0 until 3) {
      val y = topLeft.y + (5 + row * 4).dp.toPx()
      drawLine(
        ink,
        Offset(topLeft.x + 7.dp.toPx(), y),
        Offset(topLeft.x + 11.dp.toPx(), y),
        thin,
        StrokeCap.Round,
        alpha = alpha,
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun ReelProgressPreview() {
  MonTheme {
    Row(
      modifier = Modifier.padding(start = 16.dp, top = 40.dp, end = 16.dp, bottom = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      listOf(0.15f, 0.5f, 0.85f).forEach { p ->
        Box(
          Modifier.reelProgress(p, ReelPhase.Reeling)
            .size(137.dp, 176.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MonTheme.colors.primary)
        )
      }
      Box(
        Modifier.reelProgress(1f, ReelPhase.Landed)
          .size(137.dp, 176.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MonTheme.colors.primary)
      )
    }
  }
}
