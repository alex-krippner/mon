package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.akrippner.mon.core.designsystem.PillSize
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * Asks before something that cannot be undone. [confirmLabel] is the filled button and names the
 * action; [dismissLabel] keeps things as they are, as do back and a tap outside.
 */
@Composable
fun MonConfirmDialog(
  title: String,
  text: String,
  confirmLabel: String,
  dismissLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    val shape = RoundedCornerShape(12.dp)
    Column(
      modifier =
        Modifier.width(282.dp)
          .shadow(4.dp, shape)
          .background(MonTheme.colors.surfaceRaised, shape)
          .padding(20.dp)
          .semantics { paneTitle = title },
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = title,
        style = MonTheme.typography.titleMedium,
        color = MonTheme.colors.textPrimary,
      )
      Text(text = text, style = MonTheme.typography.bodySmall, color = MonTheme.colors.textPrimary)
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
      ) {
        YomuPillButton(
          label = dismissLabel,
          variant = PillVariant.Outline,
          size = PillSize.Large,
          onClick = onDismiss,
        )
        YomuPillButton(
          label = confirmLabel,
          variant = PillVariant.Filled,
          size = PillSize.Large,
          onClick = onConfirm,
        )
      }
    }
  }
}
