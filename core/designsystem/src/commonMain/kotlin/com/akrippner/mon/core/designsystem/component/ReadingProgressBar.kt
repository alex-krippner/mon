package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.ProgressVariant
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun ReadingProgressBar(progress: Float, variant: ProgressVariant, modifier: Modifier = Modifier) {
  val height: Dp =
    when (variant) {
      ProgressVariant.Card -> 3.dp
      ProgressVariant.Reader -> 3.dp
      ProgressVariant.Mini -> 2.dp
    }

  val trackColor =
    when (variant) {
      ProgressVariant.Card -> Color.White.copy(alpha = 0.15f)
      ProgressVariant.Reader -> MonTheme.colors.backgroundSecondary.copy(alpha = 0.16f)
      ProgressVariant.Mini -> MonTheme.colors.backgroundSecondary.copy(alpha = 0.09f)
    }

  val fillAlpha: Float =
    when (variant) {
      ProgressVariant.Card -> 1.0f
      ProgressVariant.Reader -> 0.70f
      ProgressVariant.Mini -> 0.75f
    }

  Box(
    modifier =
      modifier
        .then(
          if (variant == ProgressVariant.Mini) Modifier.width(160.dp) else Modifier.fillMaxWidth()
        )
        .height(height)
        .clip(RoundedCornerShape(height / 2))
  ) {
    Box(modifier = Modifier.fillMaxSize().background(trackColor))
    Box(
      modifier =
        Modifier.fillMaxHeight()
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .background(
            Brush.horizontalGradient(
              colors =
                listOf(
                  MonTheme.colors.secondary.copy(alpha = fillAlpha),
                  MonTheme.colors.accentGradientEnd.copy(alpha = fillAlpha),
                )
            )
          )
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewProgressBars() {
  MonTheme {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      ReadingProgressBar(progress = 0.36f, variant = ProgressVariant.Reader)
      ReadingProgressBar(progress = 0.36f, variant = ProgressVariant.Mini)
    }
  }
}
