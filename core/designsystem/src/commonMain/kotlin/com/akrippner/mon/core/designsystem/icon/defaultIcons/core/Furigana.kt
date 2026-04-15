package com.akrippner.mon.core.designsystem.icon.defaultIcons.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

fun Icons.Core.Furigana(primaryColor: Color = Color(0xFF000000)): ImageVector {
    return ImageVector.Builder(
        name = "Core.Furigana",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        // Dot left
        path(fill = SolidColor(primaryColor)) {
            // circle cx="280" cy="-870" r="55" — approximated as a path
            // SVG viewBox is "0 -960 960 960", so y coords are offset by 960
            // cy=-870 in SVG coords → 960 + (-870) = 90 in viewport space
            moveTo(335f, 90f)
            arcTo(55f, 55f, 0f, false, true, 280f, 145f)
            arcTo(55f, 55f, 0f, false, true, 225f, 90f)
            arcTo(55f, 55f, 0f, false, true, 335f, 90f)
            close()
        }
        // Dot center
        path(fill = SolidColor(primaryColor)) {
            moveTo(535f, 90f)
            arcTo(55f, 55f, 0f, false, true, 480f, 145f)
            arcTo(55f, 55f, 0f, false, true, 425f, 90f)
            arcTo(55f, 55f, 0f, false, true, 535f, 90f)
            close()
        }
        // Dot right
        path(fill = SolidColor(primaryColor)) {
            moveTo(735f, 90f)
            arcTo(55f, 55f, 0f, false, true, 680f, 145f)
            arcTo(55f, 55f, 0f, false, true, 625f, 90f)
            arcTo(55f, 55f, 0f, false, true, 735f, 90f)
            close()
        }
        // Original "ab" match_word icon path
        path(
            fill = SolidColor(primaryColor)
        ) {
            moveTo(40f, 761f)
            verticalLineToRelative(-200f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(120f)
            horizontalLineToRelative(720f)
            verticalLineToRelative(-120f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(200f)
            horizontalLineTo(40f)
            close()
            moveToRelative(342f, -161f)
            verticalLineToRelative(-34f)
            horizontalLineToRelative(-3f)
            quadToRelative(-13f, 20f, -35f, 31.5f)
            reflectiveQuadTo(294f, 609f)
            quadToRelative(-49f, 0f, -77f, -25.5f)
            reflectiveQuadTo(189f, 514f)
            quadToRelative(0f, -42f, 32.5f, -68.5f)
            reflectiveQuadTo(305f, 419f)
            quadToRelative(23f, 0f, 42.5f, 3.5f)
            reflectiveQuadTo(381f, 434f)
            verticalLineToRelative(-14f)
            quadToRelative(0f, -27f, -18.5f, -43f)
            reflectiveQuadTo(312f, 361f)
            quadToRelative(-21f, 0f, -39.5f, 9f)
            reflectiveQuadTo(241f, 396f)
            lineToRelative(-43f, -32f)
            quadToRelative(19f, -27f, 48f, -41f)
            reflectiveQuadToRelative(67f, -14f)
            quadToRelative(62f, 0f, 95f, 29.5f)
            reflectiveQuadToRelative(33f, 85.5f)
            verticalLineToRelative(176f)
            horizontalLineToRelative(-59f)
            close()
            moveToRelative(-66f, -134f)
            quadToRelative(-32f, 0f, -49f, 12.5f)
            reflectiveQuadTo(250f, 514f)
            quadToRelative(0f, 20f, 15f, 32.5f)
            reflectiveQuadToRelative(39f, 12.5f)
            quadToRelative(32f, 0f, 54.5f, -22.5f)
            reflectiveQuadTo(381f, 482f)
            quadToRelative(-14f, -8f, -32f, -12f)
            reflectiveQuadToRelative(-33f, -4f)
            close()
            moveToRelative(185f, 134f)
            verticalLineToRelative(-401f)
            horizontalLineToRelative(62f)
            verticalLineToRelative(113f)
            lineToRelative(-3f, 40f)
            horizontalLineToRelative(3f)
            quadToRelative(3f, -5f, 24f, -25.5f)
            reflectiveQuadToRelative(66f, -20.5f)
            quadToRelative(64f, 0f, 101f, 46f)
            reflectiveQuadToRelative(37f, 106f)
            quadToRelative(0f, 60f, -36.5f, 105.5f)
            reflectiveQuadTo(653f, 609f)
            quadToRelative(-41f, 0f, -62.5f, -18f)
            reflectiveQuadTo(563f, 563f)
            horizontalLineToRelative(-3f)
            verticalLineToRelative(37f)
            horizontalLineToRelative(-59f)
            close()
            moveToRelative(143f, -238f)
            quadToRelative(-40f, 0f, -62f, 29.5f)
            reflectiveQuadTo(560f, 457f)
            quadToRelative(0f, 37f, 22f, 66f)
            reflectiveQuadToRelative(62f, 29f)
            quadToRelative(40f, 0f, 62.5f, -29f)
            reflectiveQuadToRelative(22.5f, -66f)
            quadToRelative(0f, -37f, -22.5f, -66f)
            reflectiveQuadTo(644f, 362f)
            close()

        }
    }.build()
}
