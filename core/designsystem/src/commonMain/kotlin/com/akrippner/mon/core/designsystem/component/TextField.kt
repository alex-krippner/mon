package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.FieldVariant
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.designsystem.theme.RobotoMono

@Composable
fun YomuTextField(
  value: String, // TODO: Change to TextFieldValue?
  onValueChange: (String) -> Unit,
  label: String,
  variant: FieldVariant,
  modifier: Modifier = Modifier,
) {
  val isFocused = remember { mutableStateOf(false) }

  val borderColor =
    when {
      isFocused.value -> MonTheme.colors.borderFocus
      else -> MonTheme.colors.backgroundSecondary.copy(alpha = 0.13f)
    }

  val borderWidth = if (isFocused.value && variant == FieldVariant.Light) 1.5.dp else 1.dp

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    textStyle =
      MonTheme.typography.bodyMedium.copy(
        color = MonTheme.colors.textPrimary,
        fontFamily = RobotoMono,
      ),
    cursorBrush = SolidColor(MonTheme.colors.secondary),
    singleLine = true, // TODO: Is this visible in dark mode?
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(MonTheme.colors.surfacePrimary)
        .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
        .padding(horizontal = 16.dp, vertical = 12.dp),
    decorationBox = { innerTextField ->
      if (value.isEmpty()) {
        Text(
          text = label,
          style = MonTheme.typography.bodyMedium.copy(color = MonTheme.colors.textPrimary),
        )
      }
      innerTextField()
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
