package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.designsystem.theme.RobotoMono

@Composable
fun TextPreviewBox(text: String, wordCount: Int, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    SectionLabel(text = "PREVIEW")
    Spacer(Modifier.height(8.dp))
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .height(320.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(MonTheme.colors.backgroundSecondary.copy(alpha = 0.04f))
          .border(
            1.dp,
            MonTheme.colors.backgroundSecondary.copy(alpha = 0.08f),
            RoundedCornerShape(14.dp),
          )
    ) {
      Text(
        text = text,
        style = MonTheme.typography.bodyMedium.copy(fontFamily = RobotoMono, lineHeight = 28.sp),
        modifier = Modifier.padding(20.dp),
      )
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(64.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, MonTheme.colors.surfaceSecondary)
              )
            )
      )
    }
    Spacer(Modifier.height(10.dp))
    Box(
      modifier =
        Modifier.clip(RoundedCornerShape(13.dp))
          .background(MonTheme.colors.backgroundSecondary.copy(alpha = 0.06f))
          .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
      Text(text = "~$wordCount words detected", style = MonTheme.typography.bodyMedium)
    }
  }
}
