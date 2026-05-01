package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun YomuPageHeader(
  kanjiTitle: String,
  latinLabel: String,
  modifier: Modifier = Modifier,
  badge: @Composable () -> Unit = {},
  action: @Composable () -> Unit = {},
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MonTheme.colors.surfacePrimary,
    shadowElevation = 1.dp,
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = MonTheme.space.spacing6, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = kanjiTitle, style = MonTheme.typography.bodyMedium)
        Text(
          text = latinLabel,
          style = MonTheme.typography.bodyMedium.copy(fontSize = 9.sp, letterSpacing = 3.sp),
        )
      }
      badge()
      Spacer(Modifier.width(8.dp))
      action()
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFFDFAF4)
@Composable
private fun PreviewPageHeader() {
  MonTheme {
    YomuPageHeader(
      kanjiTitle = "本棚",
      latinLabel = "LIBRARY",
      badge = {
        Box(
          modifier =
            Modifier.background(
                MonTheme.colors.backgroundSecondary,
                androidx.compose.foundation.shape.CircleShape,
              )
              .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            "3",
            style = MonTheme.typography.bodyMedium.copy(color = MonTheme.colors.sheetTextPrimary),
          )
        }
      },
    )
  }
}
