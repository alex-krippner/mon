package com.akrippner.mon.core.designsystem

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

/**
 * True while an on-screen keyboard covers the bottom of the window. Always false on desktop. The
 * inset changes on every frame while the keyboard animates, so only a change of the answer, not of
 * the inset, recomposes the caller.
 */
@Composable
fun isKeyboardOpen(): Boolean {
  val ime = WindowInsets.ime
  val density = LocalDensity.current
  val isOpen by remember(ime, density) { derivedStateOf { ime.getBottom(density) > 0 } }
  return isOpen
}
