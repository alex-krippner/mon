package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun FilterPill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bgColor =
    when {
      !selected -> MonTheme.colors.filterPillDefault.background
      else -> MonTheme.colors.filterPillSelected.background
    }
  val textColor =
    when {
      !selected -> MonTheme.colors.filterPillDefault.text
      else -> MonTheme.colors.filterPillSelected.text
    }
  val borderColor =
    when {
      !selected -> Color.Transparent

      else -> MonTheme.colors.filterPillSelected.border
    }

  val borderWidth =
    when {
      !selected -> 0.dp
      else -> 1.dp
    }

  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(11.dp))
        .background(bgColor)
        .border(borderWidth, borderColor, RoundedCornerShape(11.dp))
        .clickable(
          interactionSource =
            remember {
              MutableInteractionSource()
            }, // TODO: Check why interactionSource is set here
          indication = ripple(bounded = true),
          onClick = onClick,
        )
        .padding(horizontal = 14.dp, vertical = 5.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(text = label, style = MonTheme.typography.labelSmall.copy(color = textColor))
  }
}

@Preview
@Composable
private fun FilterPillPreview() {
  MonTheme {
    Row(
      modifier = Modifier.background(MonTheme.colors.appBackground).padding(10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterPill(label = "N5", selected = true, onClick = {})
      FilterPill(label = "N4", selected = true, onClick = {})
      FilterPill(label = "N3", selected = false, onClick = {})
    }
  }
}
