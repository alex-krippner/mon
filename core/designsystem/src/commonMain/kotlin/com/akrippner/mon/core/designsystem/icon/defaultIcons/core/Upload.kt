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

/** Material `file_upload`, filled. */
fun Icons.Core.Upload(primaryColor: Color = Color(0xFF000000)): ImageVector {
  return ImageVector.Builder(
      name = "Core.Upload",
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
        moveTo(9.0f, 16.0f)
        horizontalLineToRelative(6.0f)
        verticalLineToRelative(-6.0f)
        horizontalLineToRelative(4.0f)
        lineToRelative(-7.0f, -7.0f)
        lineToRelative(-7.0f, 7.0f)
        horizontalLineToRelative(4.0f)
        close()
        moveTo(5.0f, 18.0f)
        horizontalLineToRelative(14.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(5.0f)
        close()
      }
    }
    .build()
}
