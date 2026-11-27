package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

/** An outlined bookmark: a word that is not saved. */
fun Icons.Core.Bookmark(primaryColor: Color = Color(0xFF000000)): ImageVector {
  return ImageVector.Builder(
      name = "Core.Bookmark",
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
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter,
      ) {
        moveTo(6f, 20f)
        verticalLineTo(5f)
        curveTo(6f, 4.45f, 6.45f, 4f, 7f, 4f)
        horizontalLineTo(17f)
        curveTo(17.55f, 4f, 18f, 4.45f, 18f, 5f)
        verticalLineTo(20f)
        lineTo(12f, 17.4f)
        lineTo(6f, 20f)
        close()
      }
    }
    .build()
}
