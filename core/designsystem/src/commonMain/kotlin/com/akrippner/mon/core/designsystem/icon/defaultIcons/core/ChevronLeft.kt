package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

fun Icons.Core.ChevronLeft(primaryColor: Color = Color(0xFF000000)): ImageVector {
  return ImageVector.Builder(
      name = "Core.ChevronLeft",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    )
    .apply {
      path(
        fill = SolidColor(primaryColor), // Using your specific SVG color
        fillAlpha = 1.0f,
        stroke = null,
        strokeLineWidth = 0.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 4.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(15.41f, 7.41f)
        lineTo(14.0f, 6.0f)
        lineToRelative(-6.0f, 6.0f)
        lineToRelative(6.0f, 6.0f)
        lineToRelative(1.41f, -1.41f)
        lineTo(10.83f, 12.0f)
        close()
      }
    }
    .build()
}
