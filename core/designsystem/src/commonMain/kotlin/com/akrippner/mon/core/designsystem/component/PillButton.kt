package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.PillSize
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.stateLayer
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * A fully rounded button. [PillSize.Compact] is the dense 30 dp pill; [PillSize.Large] is 44 dp
 * high (48 with a [sublabel]) with hover, pressed and focus states, for screen actions. A
 * [leadingIcon] is shown only at [PillSize.Large].
 */
@Composable
fun YomuPillButton(
  label: String,
  variant: PillVariant,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  sublabel: String? = null,
  enabled: Boolean = true,
  size: PillSize = PillSize.Compact,
  leadingIcon: IconId? = null,
) {
  when (size) {
    PillSize.Compact -> CompactPill(label, variant, onClick, modifier, sublabel, enabled)
    PillSize.Large -> LargePill(label, variant, onClick, modifier, sublabel, enabled, leadingIcon)
  }
}

@Composable
private fun CompactPill(
  label: String,
  variant: PillVariant,
  onClick: () -> Unit,
  modifier: Modifier,
  sublabel: String?,
  enabled: Boolean,
) {
  val bgColor =
    when {
      !enabled -> MonTheme.colors.textPrimary.copy(alpha = 0.08f)
      variant == PillVariant.Filled -> MonTheme.colors.secondary
      else -> Color.Transparent
    }
  val labelColor =
    when {
      !enabled -> MonTheme.colors.textPrimary.copy(alpha = 0.25f)
      variant == PillVariant.Filled -> Color.White
      else -> MonTheme.colors.textSecondary
    }
  val borderColor =
    when {
      variant == PillVariant.Outline -> MonTheme.colors.textPrimary.copy(alpha = 0.25f)
      else -> Color.Transparent
    }
  val shape = RoundedCornerShape(if (sublabel !== null) 14.dp else 15.dp)
  val height = if (sublabel != null) 36.dp else 30.dp

  Box(
    modifier =
      modifier
        .height(height)
        .clip(shape)
        .background(bgColor)
        .border(1.5.dp, borderColor, shape)
        .clickable(
          enabled = enabled,
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(bounded = true),
          onClick = onClick,
        )
        .padding(horizontal = 18.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        style = MonTheme.typography.bodyMedium.copy(fontSize = 11.sp, color = labelColor),
      )
      if (sublabel != null) {
        Text(
          text = sublabel,
          style =
            MonTheme.typography.bodyMedium.copy(
              fontSize = 8.sp,
              color = labelColor.copy(alpha = 0.70f),
            ),
        )
      }
    }
  }
}

@Composable
private fun LargePill(
  label: String,
  variant: PillVariant,
  onClick: () -> Unit,
  modifier: Modifier,
  sublabel: String?,
  enabled: Boolean,
  leadingIcon: IconId?,
) {
  val colors = MonTheme.colors
  // paper60 is used only for disabled controls, which are exempt from the contrast rule.
  val disabledContent = colors.listItem.subtitle
  val background =
    when {
      variant == PillVariant.Outline -> Color.Transparent
      enabled -> colors.secondary
      else -> colors.primary
    }
  val content =
    when {
      !enabled -> disabledContent
      variant == PillVariant.Filled -> Color.White
      else -> colors.textPrimary
    }
  val shape = RoundedCornerShape(percent = 50)
  val interactionSource = remember { MutableInteractionSource() }
  val padding =
    if (leadingIcon != null) PaddingValues(start = 14.dp, end = 18.dp)
    else PaddingValues(horizontal = 18.dp)

  Row(
    modifier =
      modifier
        .height(if (sublabel != null) 48.dp else 44.dp)
        .then(if (enabled) Modifier.stateLayer(interactionSource, shape) else Modifier)
        .clip(shape)
        .background(background)
        .then(
          if (variant == PillVariant.Outline) {
            Modifier.border(1.dp, if (enabled) colors.textPrimary else disabledContent, shape)
          } else {
            Modifier
          }
        )
        .clickable(
          enabled = enabled,
          interactionSource = interactionSource,
          indication = null,
          role = Role.Button,
          onClick = onClick,
        )
        .padding(padding),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (leadingIcon != null) {
      MonIcon(id = leadingIcon, primaryColor = content, requiredSize = 18.dp)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        style = MonTheme.typography.labelMedium,
        color = content,
        textAlign = TextAlign.Center,
      )
      if (sublabel != null) {
        Text(text = sublabel, style = MonTheme.typography.labelSmall, color = content)
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewPillButtons() {
  MonTheme {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        YomuPillButton(label = "読む", variant = PillVariant.Filled, onClick = {})
        YomuPillButton(label = "保存", sublabel = "Save", variant = PillVariant.Filled, onClick = {})
        YomuPillButton(label = "Cancel", variant = PillVariant.Outline, onClick = {})
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        YomuPillButton(
          label = "Download",
          variant = PillVariant.Filled,
          size = PillSize.Large,
          onClick = {},
        )
        YomuPillButton(
          label = "Check again",
          variant = PillVariant.Outline,
          size = PillSize.Large,
          leadingIcon = Icon.Core.Refresh,
          onClick = {},
        )
      }
    }
  }
}
