package com.akrippner.mon.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.akrippner.mon.core.designsystem.icon.defaultIcons.DefaultIconCatalog
import com.akrippner.mon.core.designsystem.icon.defaultIcons.IconCatalog
import com.akrippner.mon.core.designsystem.icon.defaultIcons.Icons

val LocalIcons = staticCompositionLocalOf { Icons }
val LocalIconCatalog = staticCompositionLocalOf<IconCatalog> { DefaultIconCatalog }

@Composable
fun MonIcons(iconCatalog: IconCatalog = DefaultIconCatalog, content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalIcons provides Icons, LocalIconCatalog provides iconCatalog) {
    content()
  }
}
