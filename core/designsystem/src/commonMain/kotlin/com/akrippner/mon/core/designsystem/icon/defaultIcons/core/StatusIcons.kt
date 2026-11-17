package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

// Material Symbols (filled, weight 400) for status rows, notices and the refresh button. The path
// data is copied from the symbols as published, in a 24 × 24 viewport.

/** `check_circle`: something is done or installed. */
fun Icons.Core.CheckCircle(primaryColor: Color = Color(0xFF000000)): ImageVector =
  symbol(
    "Core.CheckCircle",
    primaryColor,
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41" +
      "L10 14.17l7.59-7.59L19 8l-9 9z",
  )

/** `error`: something failed. */
fun Icons.Core.ErrorCircle(primaryColor: Color = Color(0xFF000000)): ImageVector =
  symbol(
    "Core.ErrorCircle",
    primaryColor,
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z",
  )

/** `info`: a quiet notice. */
fun Icons.Core.Info(primaryColor: Color = Color(0xFF000000)): ImageVector =
  symbol(
    "Core.Info",
    primaryColor,
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z",
  )

/** `block`: something this app cannot use. */
fun Icons.Core.Block(primaryColor: Color = Color(0xFF000000)): ImageVector =
  symbol(
    "Core.Block",
    primaryColor,
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zM4 12c0-4.42 3.58-8 8-8 " +
      "1.85 0 3.55.63 4.9 1.69L5.69 16.9C4.63 15.55 4 13.85 4 12zm8 8c-1.85 0-3.55-.63-4.9-1.69" +
      "L18.31 7.1C19.37 8.45 20 10.15 20 12c0 4.42-3.58 8-8 8z",
  )

/** `refresh`: check again. */
fun Icons.Core.Refresh(primaryColor: Color = Color(0xFF000000)): ImageVector =
  symbol(
    "Core.Refresh",
    primaryColor,
    "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 " +
      "7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78" +
      "L13 11h7V4l-2.35 2.35z",
  )

private fun symbol(name: String, color: Color, pathData: String): ImageVector =
  ImageVector.Builder(
      name = name,
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    )
    .addPath(pathData = addPathNodes(pathData), fill = SolidColor(color))
    .build()
