package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun FloatingActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  FloatingActionButton(
    onClick = onClick,
    shape = CircleShape,
    containerColor = MonTheme.colors.buttonPrimary.background,
    contentColor = MonTheme.colors.buttonPrimary.icon,
    elevation =
      FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 12.dp),
  ) {
    MonIcon(
      id = Icon.Core.Plus,
      size = IconSize.Small,
      primaryColor = MonTheme.colors.buttonPrimary.icon,
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewFloatingActionButton() {
  MonTheme { Surface { FloatingActionButton(onClick = {}, modifier = Modifier.padding(16.dp)) } }
}
