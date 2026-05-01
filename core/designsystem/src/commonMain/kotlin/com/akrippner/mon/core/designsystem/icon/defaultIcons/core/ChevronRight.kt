package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

fun Icons.Core.ChevronRight(primaryColor: Color = Color(0xFF000000)): ImageVector {
  return ImageVector.Builder(
      name = "Core.ChevronRight",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    )
    .apply {
      path(fill = SolidColor(primaryColor), pathFillType = PathFillType.NonZero) {
        moveTo(10.0f, 6.0f)
        lineTo(8.59f, 7.41f)
        lineTo(13.17f, 12.0f)
        lineToRelative(-4.58f, 4.59f)
        lineTo(10.0f, 18.0f)
        lineToRelative(6.0f, -6.0f)
        close()
      }
    }
    .build()
}
