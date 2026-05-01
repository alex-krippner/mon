package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun AnnotationDot(modifier: Modifier = Modifier, size: Dp = 12.dp) {

  Box(
    modifier = modifier.size(size).clip(CircleShape).background(MonTheme.colors.secondary),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "✓",
      style =
        MonTheme.typography.labelSmall.copy(
          color = MonTheme.colors.sheetTextPrimary,
          fontSize = (size.value * 0.55f).sp,
        ),
      textAlign = TextAlign.Center,
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8) // TODO: use d
@Composable
private fun PreviewAnnotationDot() {
  MonTheme {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      AnnotationDot(size = 12.dp)
      AnnotationDot(size = 16.dp)
    }
  }
}
