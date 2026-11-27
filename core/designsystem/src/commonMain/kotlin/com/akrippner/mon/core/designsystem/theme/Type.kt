package com.akrippner.mon.core.designsystem.theme

import androidx.compose.ui.text.TextStyle

data class MonThemeData(
  val colors: MonColors,
  val typography: TypographyTokens,
  val radii: Radii,
  val sizes: Sizes,
  val space: Space,
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
  /** 11 sp regular: kana and sentences on a vocabulary card. */
  val caption: TextStyle,
  /** 13 sp bold: the text of a full-width pill button. */
  val labelButton: TextStyle,
  /** 16 sp bold: the title of a pushed screen's header. */
  val titleCompact: TextStyle,
)
