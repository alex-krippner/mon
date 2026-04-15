package com.akrippner.mon.core.designsystem.theme

import androidx.compose.ui.text.TextStyle

data class MonThemeData(
    val colors: MonColors,
    val typography: TypographyTokens,
    val sizes: Sizes,
    val space: Space
)

data class TypographyTokens(
    val bodyExtraSmall: TextStyle,
    val bodySmall: TextStyle,
    val bodyMedium: TextStyle,
    val bodyLarge: TextStyle,
    val labelSmall: TextStyle,
    val labelMedium: TextStyle,
    val labelLarge: TextStyle,
    val titleSmall: TextStyle,
    val titleMedium: TextStyle,
    val titleLarge: TextStyle,
    val displaySmall: TextStyle,
    val displayMedium: TextStyle,
    val displayLarge: TextStyle,
    val headlineSmall: TextStyle,
    val headlineMedium: TextStyle,
    val headlineLarge: TextStyle,
)