package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

fun Icons.Core.Plus(primaryColor: Color = Color(0xFF000000)): ImageVector {
   return ImageVector.Builder(
        name = "Core.Plus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
   ).apply{
       path(
           fill = SolidColor(Color.Transparent),
           stroke = SolidColor(primaryColor),
           strokeLineWidth = 2f,
           strokeLineCap = StrokeCap.Round,
           strokeLineJoin = StrokeJoin.Round
       ) {
           moveTo(12f, 5f)
           lineTo(12f, 19f)
       }
       path(
           fill = SolidColor(Color.Transparent),
           stroke = SolidColor(primaryColor),
           strokeLineWidth = 2f,
           strokeLineCap = StrokeCap.Round,
           strokeLineJoin = StrokeJoin.Round
       ) {
           moveTo(5f, 12f)
           lineTo(19f, 12f)
       }
   }.build()
}