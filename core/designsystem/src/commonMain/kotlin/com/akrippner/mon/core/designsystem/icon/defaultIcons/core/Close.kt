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

/** Material Symbols `close`, filled, weight 400. */
fun Icons.Core.Close(primaryColor: Color = Color(0xFF000000)): ImageVector {
  return ImageVector.Builder(
      name = "Core.Close",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    )
    .apply {
      path(
        fill = SolidColor(primaryColor),
        fillAlpha = 1.0f,
        stroke = null,
        strokeLineWidth = 0.0f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
        strokeLineMiter = 4.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(6.4f, 19.0f)
        lineTo(5.0f, 17.6f)
        lineTo(10.6f, 12.0f)
        lineTo(5.0f, 6.4f)
        lineTo(6.4f, 5.0f)
        lineTo(12.0f, 10.6f)
        lineTo(17.6f, 5.0f)
        lineTo(19.0f, 6.4f)
        lineTo(13.4f, 12.0f)
        lineTo(19.0f, 17.6f)
        lineTo(17.6f, 19.0f)
        lineTo(12.0f, 13.4f)
        lineTo(6.4f, 19.0f)
        close()
      }
    }
    .build()
}
