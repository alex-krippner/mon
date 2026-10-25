package com.akrippner.mon.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Sizes(
  val iconExtraSmall: Dp,
  val iconSmall: Dp,
  val iconMedium: Dp,
  val iconLarge: Dp,
  val iconExtraLarge: Dp,
  val buttonSmall: Dp,
  val buttonMedium: Dp,
  val buttonLarge: Dp,
)

val sizes =
  Sizes(
    iconExtraSmall = 24.dp,
    iconSmall = 32.dp,
    iconMedium = 48.dp,
    iconLarge = 64.dp,
    iconExtraLarge = 80.dp,
    buttonSmall = 24.dp,
    buttonMedium = 32.dp,
    buttonLarge = 48.dp,
  )
