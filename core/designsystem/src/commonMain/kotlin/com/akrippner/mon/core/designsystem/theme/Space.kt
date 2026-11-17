package com.akrippner.mon.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Space(
  val spacing1px: Dp,
  val spacing2px: Dp,
  val spacing1: Dp,
  val spacing2: Dp,
  val spacing3: Dp,
  val spacing4: Dp,
  val spacing5: Dp,
  val spacing6: Dp,
  val spacing7: Dp,
  val spacing8: Dp,
  val spacing9: Dp,
  val spacing10: Dp,
  val spacing11: Dp,
  val spacing12: Dp,
  val spacing13: Dp,
  val spacing14: Dp,
  val spacing15: Dp,
  val headlineGap: Dp,
  /** Horizontal margin of a screen's content, from the design spec. */
  val screenMargin: Dp,
)

val space =
  Space(
    spacing1px = 1.dp,
    spacing2px = 2.dp,
    spacing1 = 4.dp,
    spacing2 = 8.dp,
    spacing3 = 12.dp,
    spacing4 = 16.dp,
    spacing5 = 20.dp,
    spacing6 = 24.dp,
    spacing7 = 32.dp,
    spacing8 = 40.dp,
    spacing9 = 56.dp,
    spacing10 = 64.dp,
    spacing11 = 80.dp,
    spacing12 = 96.dp,
    spacing13 = 112.dp,
    spacing14 = 120.dp,
    spacing15 = 128.dp,
    headlineGap = 14.dp,
    screenMargin = 39.dp,
  )
