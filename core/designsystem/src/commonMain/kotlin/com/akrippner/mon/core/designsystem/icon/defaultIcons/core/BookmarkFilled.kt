package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

/** A filled bookmark: a saved word. */
fun Icons.Core.BookmarkFilled(primaryColor: Color = Color(0xFF000000)): ImageVector {
  return ImageVector.Builder(
      name = "Core.BookmarkFilled",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    )
    .apply {
      path(fill = SolidColor(primaryColor)) {
        moveTo(5f, 21f)
        verticalLineTo(5f)
        curveTo(5f, 4.45f, 5.2f, 3.98f, 5.59f, 3.59f)
        curveTo(5.98f, 3.2f, 6.45f, 3f, 7f, 3f)
        horizontalLineTo(17f)
        curveTo(17.55f, 3f, 18.02f, 3.2f, 18.41f, 3.59f)
        curveTo(18.8f, 3.98f, 19f, 4.45f, 19f, 5f)
        verticalLineTo(21f)
        lineTo(12f, 18f)
        lineTo(5f, 21f)
        close()
      }
    }
    .build()
}
