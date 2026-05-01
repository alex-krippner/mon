package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun PosBadge(pos: String, modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(4.dp))
        .background(Color.White.copy(alpha = 0.07f))
        .padding(horizontal = 8.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = pos,
      style =
        MonTheme.typography.bodyMedium.copy(
          color = Color.White.copy(alpha = 0.50f),
          fontWeight = FontWeight.Normal,
        ),
    )
  }
}
