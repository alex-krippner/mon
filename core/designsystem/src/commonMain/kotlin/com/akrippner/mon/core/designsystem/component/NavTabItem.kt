package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun YomuNavTabItem(
  iconId: IconId,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val contentColor =
    if (selected) MonTheme.colors.navigation.selected else MonTheme.colors.navigation.default
  val backgroundColor =
    if (selected) MonTheme.colors.navigation.selectedBackground else Color.Transparent

  Box(
    modifier =
      modifier
        .width(40.dp)
        .height(32.dp)
        .clip(RoundedCornerShape(20))
        .clickable(onClick = onClick)
        .background(backgroundColor),
    contentAlignment = Alignment.Center,
  ) {
    MonIcon(id = iconId, primaryColor = contentColor, size = IconSize.ExtraSmall)
  }
}
