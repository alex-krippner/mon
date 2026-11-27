package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * The design's furigana pill, 33×19 dp. Its touch target is still at least 48 dp: the layout grows
 * around the pill, so a caller that aligns the pill to an edge must make up the difference.
 */
@Composable
fun FuriganaToggle(enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
  val containerColor =
    if (enabled) MonTheme.colors.furiganaToggle.background
    else MonTheme.colors.furiganaToggle.disabled

  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .size(width = 33.dp, height = 19.dp)
        .clip(CircleShape)
        .background(containerColor)
        .clickable(role = Role.Button, onClick = onToggle),
    contentAlignment = Alignment.Center,
  ) {
    MonIcon(
      id = Icon.Core.Furigana,
      requiredSize = 15.dp, // the design's icon size; the pill is only 19 dp high
      primaryColor = MonTheme.colors.furiganaToggle.icon,
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewFuriganaToggle() {
  MonTheme {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      FuriganaToggle(enabled = true, onToggle = {})
      FuriganaToggle(enabled = false, onToggle = {})
    }
  }
}
