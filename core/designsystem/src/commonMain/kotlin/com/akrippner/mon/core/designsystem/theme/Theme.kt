package com.akrippner.mon.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalMonTheme =
  staticCompositionLocalOf<MonThemeData> {
    error("CompositionLocal MonColors not present - did you forget to add MonTheme?")
  }

object MonTheme {
  val colors: MonColors
    @Composable get() = LocalMonTheme.current.colors

  val typography: TypographyTokens
    @Composable get() = LocalMonTheme.current.typography

  val sizes: Sizes
    @Composable get() = LocalMonTheme.current.sizes

  val space: Space
    @Composable get() = LocalMonTheme.current.space
}

@Composable
fun MonTheme(content: @Composable () -> Unit) {
  val colors = lightMonColors
  val theme =
    MonThemeData(colors = colors, typography = MonTypography, sizes = sizes, space = space)

  CompositionLocalProvider(LocalMonTheme provides theme) { content() }
}
