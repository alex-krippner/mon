package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

fun Icons.Core.Vocabulary(primaryColor: Color = Color(0xFF000000)): ImageVector {
  return ImageVector.Builder(
      name = "Core.Vocabulary",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    )
    .apply {
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(primaryColor),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(10f, 19f)
        horizontalLineToRelative(-6f)
        arcToRelative(1f, 1f, 0f, false, true, -1f, -1f)
        verticalLineToRelative(-14f)
        arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
        horizontalLineToRelative(6f)
        arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
        arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
        horizontalLineToRelative(6f)
        arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
        verticalLineToRelative(14f)
        arcToRelative(1f, 1f, 0f, false, true, -1f, 1f)
        horizontalLineToRelative(-6f)
        arcToRelative(2f, 2f, 0f, false, false, -2f, 2f)
        arcToRelative(2f, 2f, 0f, false, false, -2f, -2f)
        close()
      }
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(primaryColor),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 5f)
        verticalLineToRelative(16f)
      }
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(primaryColor),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(7f, 7f)
        horizontalLineToRelative(1f)
      }
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(primaryColor),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(7f, 11f)
        horizontalLineToRelative(1f)
      }
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(primaryColor),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 7f)
        horizontalLineToRelative(1f)
      }
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(primaryColor),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 11f)
        horizontalLineToRelative(1f)
      }
      path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(primaryColor),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 15f)
        horizontalLineToRelative(1f)
      }
    }
    .build()
}
