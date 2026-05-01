package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun HomeIndicator(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .size(width = 90.dp, height = 4.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(MonTheme.colors.backgroundSecondary.copy(alpha = 0.20f))
  )
}
