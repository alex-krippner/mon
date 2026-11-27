package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.FieldVariant
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.designsystem.theme.RobotoMono

/**
 * A single-line field with a [label] shown while it is empty. The light variant takes its edge and
 * placeholder from the input colours; a locked field ([enabled] false) keeps its look, so callers
 * dim it themselves.
 */
@Composable
fun YomuTextField(
  value: String, // TODO: Change to TextFieldValue?
  onValueChange: (String) -> Unit,
  label: String,
  variant: FieldVariant,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  val isFocused = remember { mutableStateOf(false) }
  val light = variant == FieldVariant.Light

  val borderColor =
    when {
      // The focus border is part of the light design only; the dark field never showed one.
      isFocused.value && light -> MonTheme.colors.borderFocus
      light -> MonTheme.colors.input.border
      else -> MonTheme.colors.backgroundSecondary.copy(alpha = 0.13f)
    }
  val placeholderColor =
    if (light) MonTheme.colors.input.placeholder else MonTheme.colors.textPrimary

  val borderWidth = if (isFocused.value && light) 2.dp else 1.dp

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    textStyle =
      MonTheme.typography.bodyMedium.copy(
        color = MonTheme.colors.textPrimary,
        fontFamily = RobotoMono,
      ),
    cursorBrush = SolidColor(MonTheme.colors.secondary),
    singleLine = true, // TODO: Is this visible in dark mode?
    modifier =
      modifier
        .onFocusChanged { isFocused.value = it.isFocused }
        .heightIn(min = 40.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(MonTheme.colors.surfacePrimary)
        .border(borderWidth, borderColor, RoundedCornerShape(12.dp)),
    decorationBox = { innerTextField ->
      // The padding is inside the field, so a tap anywhere in the box focuses it.
      Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        if (value.isEmpty()) {
          Text(text = label, style = MonTheme.typography.bodyMedium.copy(color = placeholderColor))
        }
        innerTextField()
      }
    },
  )
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewTextFields() {
  MonTheme {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      YomuTextField(value = "", onValueChange = {}, label = "Title…", variant = FieldVariant.Light)
      YomuTextField(
        value = "こころ",
        onValueChange = {},
        label = "Title…",
        variant = FieldVariant.Light,
      )
    }
  }
}
