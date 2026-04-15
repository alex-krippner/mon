package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

fun Icons.Core.Search(primaryColor: Color = Color(0xFF000000)): ImageVector {
    return ImageVector.Builder(
        name = "Core.Search",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(
            fill = SolidColor(primaryColor),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(15.5f, 14.0f)
            horizontalLineToRelative(-0.79f)
            lineToRelative(-0.28f, -0.27f)
            curveTo(15.41f, 12.59f, 16.0f, 11.11f, 16.0f, 9.5f)
            curveTo(16.0f, 5.91f, 13.09f, 3.0f, 9.5f, 3.0f)
            reflectiveCurveTo(3.0f, 5.91f, 3.0f, 9.5f)
            reflectiveCurveTo(5.91f, 16.0f, 9.5f, 16.0f)
            curveToRelative(1.61f, 0.0f, 3.09f, -0.59f, 4.23f, -1.57f)
            lineToRelative(0.27f, 0.28f)
            verticalLineToRelative(0.79f)
            lineToRelative(5.0f, 4.99f)
            lineTo(20.49f, 19.0f)
            lineToRelative(-4.99f, -5.0f)
            close()
            moveTo(9.5f, 14.0f)
            curveTo(7.01f, 14.0f, 5.0f, 11.99f, 5.0f, 9.5f)
            reflectiveCurveTo(7.01f, 5.0f, 9.5f, 5.0f)
            reflectiveCurveTo(14.0f, 7.01f, 14.0f, 9.5f)
            reflectiveCurveTo(11.99f, 14.0f, 9.5f, 14.0f)
            close()
        }
    }.build()
}