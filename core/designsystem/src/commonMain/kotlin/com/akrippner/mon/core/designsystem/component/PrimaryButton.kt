package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.modifier.conditional
import com.akrippner.mon.core.designsystem.theme.MonTheme

enum class ButtonVariant {
  Primary,
  Secondary,
}

enum class ButtonSize {
  Small,
  Medium,
  Large,
}

@Composable
fun PrimaryButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  enabled: Boolean = true,
) {
  val bg =
    if (enabled) {
      Brush.horizontalGradient(
        colors = listOf(MonTheme.colors.secondary, MonTheme.colors.accentGradientEnd)
      )
    } else {
      Brush.horizontalGradient(
        colors =
          listOf(
            MonTheme.colors.backgroundSecondary.copy(alpha = 0.13f),
            MonTheme.colors.backgroundSecondary.copy(alpha = 0.13f),
          )
      )
    }

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(if (subtitle != null) 56.dp else 48.dp)
        .clip(RoundedCornerShape(16.dp)) // TODO: Define universal sizes
        .background(bg)
        .clickable(enabled = enabled, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        style =
          MonTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            color =
              if (enabled) MonTheme.colors.sheetTextPrimary
              else MonTheme.colors.textPrimary.copy(alpha = 0.25f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
          ),
      )
      if (subtitle != null) {
        Spacer(Modifier.height(2.dp))
        Text(
          text = subtitle,
          style =
            MonTheme.typography.bodyMedium.copy(
              color =
                if (enabled) Color.White.copy(alpha = 0.65f)
                else MonTheme.colors.textPrimary.copy(alpha = 0.20f),
              fontSize = 9.sp,
            ),
        )
      }
    }
  }
}

@Composable
fun Button(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  variant: ButtonVariant = ButtonVariant.Primary,
  size: ButtonSize = ButtonSize.Medium,
  enabled: Boolean = true,
  loading: Boolean = false,
  startIcon: IconId? = null,
  endIcon: IconId? = null,
  iconColor: Color? = null,
) {

  val isIconOnly = label == null
  val backgroundColor =
    when (variant) {
      ButtonVariant.Primary -> MonTheme.colors.buttonPrimary.background
      ButtonVariant.Secondary -> MonTheme.colors.buttonSecondary.background
    }

  val contentColor =
    when (variant) {
      ButtonVariant.Primary -> MonTheme.colors.buttonPrimary.text
      ButtonVariant.Secondary -> MonTheme.colors.buttonSecondary.text
    }

  val buttonHeight =
    when (size) {
      ButtonSize.Small -> MonTheme.sizes.buttonSmall
      ButtonSize.Medium -> MonTheme.sizes.buttonMedium
      ButtonSize.Large -> MonTheme.sizes.buttonLarge
    }

  val horizontalPadding =
    when (size) {
      ButtonSize.Small -> MonTheme.space.spacing4
      ButtonSize.Medium -> MonTheme.space.spacing5
      ButtonSize.Large -> MonTheme.space.spacing6
    }

  val verticalPadding =
    when (size) {
      ButtonSize.Small -> MonTheme.space.spacing4
      ButtonSize.Medium -> MonTheme.space.spacing4
      ButtonSize.Large -> MonTheme.space.spacing5
    }

  val shape = RoundedCornerShape(MonTheme.radii.button)

  val buttonModifier =
    modifier
      .clip(shape)
      .conditional(isIconOnly) { height(buttonHeight) }
      .background(backgroundColor)
      .padding(horizontal = horizontalPadding, vertical = verticalPadding)
      .conditional(enabled) { clickable(onClick = onClick) }

  Box(modifier = buttonModifier, contentAlignment = Alignment.Center) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      startIcon?.let { id ->
        MonIcon(
          id = id,
          contentDescription = null,
          primaryColor = iconColor ?: contentColor,
          size = IconSize.Small,
        )
      }
      label?.let { text ->
        Text(
          text = text,
          color = contentColor,
          style = MonTheme.typography.labelLarge,
          maxLines = 1,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = MonTheme.space.spacing3),
        )
      }
      endIcon?.let { id ->
        MonIcon(
          id = id,
          contentDescription = null,
          primaryColor = iconColor ?: contentColor,
          size = IconSize.Small,
        )
      }
    }
  }
}

@Preview
@Composable
fun ButtonPreview() {
  MonTheme { Button(label = "Save", onClick = {}, enabled = true) }
}
