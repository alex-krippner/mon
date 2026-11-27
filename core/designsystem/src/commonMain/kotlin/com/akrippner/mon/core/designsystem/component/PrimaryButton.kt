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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.conditional
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
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

/**
 * The screen's main action: a flat, full-width button. While [busy] it shows a progress ring and
 * does not react to taps, but keeps its colours so the label still reads as the action in progress.
 */
@Composable
fun PrimaryButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  enabled: Boolean = true,
  busy: Boolean = false,
) {
  val colors =
    if (enabled || busy) MonTheme.colors.buttonPrimary else MonTheme.colors.buttonDisabled

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(if (subtitle != null) 56.dp else 48.dp)
        .clip(RoundedCornerShape(16.dp)) // TODO: Define universal sizes
        .background(colors.background)
        .clickable(enabled = enabled && !busy, onClick = onClick)
        .semantics { if (busy) stateDescription = label },
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (busy) ProgressRing(color = colors.text, size = 18.dp)
        Text(text = label, style = MonTheme.typography.titleMedium.copy(color = colors.text))
      }
      if (subtitle != null) {
        Spacer(Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MonTheme.typography.bodyMedium.copy(color = colors.text, fontSize = 9.sp),
        )
      }
    }
  }
}

@Composable
private fun ProgressRing(color: Color, size: Dp) {
  CircularProgressIndicator(
    modifier = Modifier.size(size),
    color = color,
    trackColor = color.copy(alpha = 0.35f),
    strokeWidth = 2.dp,
  )
}

/**
 * A button in the design's three sizes. A [Small] button is a 24 dp pill inside a touch target of
 * at least 48 dp. While [loading] it shows a progress ring and keeps its colours; otherwise a
 * disabled button turns grey. [shape] defaults to fully rounded.
 */
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
  shape: Shape = RoundedCornerShape(MonTheme.radii.button),
) {
  val colors =
    when {
      !enabled && !loading -> MonTheme.colors.buttonDisabled
      variant == ButtonVariant.Primary -> MonTheme.colors.buttonPrimary
      else -> MonTheme.colors.buttonSecondary
    }
  val contentColor = colors.text

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

  val labelStyle =
    when (size) {
      ButtonSize.Small -> MonTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
      else -> MonTheme.typography.labelLarge
    }

  // The clickable area is the outer box, so it can be larger than the drawn button.
  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .conditional(enabled && !loading) { clickable(onClick = onClick) }
        .semantics { if (loading) stateDescription = label.orEmpty() },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier.height(buttonHeight)
          .clip(shape)
          .background(colors.background)
          .padding(horizontal = horizontalPadding),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (loading) ProgressRing(color = contentColor, size = 14.dp)
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
            style = labelStyle,
            maxLines = 1,
            textAlign = TextAlign.Center,
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
}

@Preview
@Composable
fun ButtonPreview() {
  MonTheme { Button(label = "Save", onClick = {}, enabled = true) }
}
