package com.akrippner.mon.core.designsystem.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
inline fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier =
  if (condition) this.modifier() else this
