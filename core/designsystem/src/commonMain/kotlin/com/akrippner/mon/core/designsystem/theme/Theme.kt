package com.akrippner.mon.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────
// Spacing tokens
// ─────────────────────────────────────────────

object YomuSpacing {
    val xs:  Dp = 4.dp
    val sm:  Dp = 8.dp
    val md:  Dp = 16.dp
    val lg:  Dp = 24.dp
    val xl:  Dp = 32.dp
    val xxl: Dp = 48.dp

    val screenPadding: Dp = 24.dp
    val cardRadius:    Dp = 16.dp
    val sheetRadius:   Dp = 24.dp
}

// ─────────────────────────────────────────────
// MaterialTheme wrapper
// ─────────────────────────────────────────────

private val YomuColorScheme = lightColorScheme(
    primary         = YomuColors.Vermilion,
    onPrimary       = Color.White,
    background      = YomuColors.Paper,
    onBackground    = YomuColors.Ink,
    surface         = YomuColors.Paper,
    onSurface       = YomuColors.Ink,
    surfaceVariant  = YomuColors.Canvas,
    outline         = YomuColors.InkHairline,
)

@Composable
fun YomuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YomuColorScheme,
        content     = content,
    )
}
