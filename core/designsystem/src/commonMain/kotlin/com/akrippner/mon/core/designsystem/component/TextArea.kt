package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.designsystem.theme.RobotoMono

private val AREA_SHAPE = RoundedCornerShape(25.dp)

/**
 * A multi-line text area for long text, such as a pasted chapter. Give it a bounded height (for
 * example `Modifier.weight(1f)`): it then scrolls inside itself. Shows [placeholder] while empty
 * and a focus ring while focused.
 */
@Composable
fun MonTextArea(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  var isFocused by remember { mutableStateOf(false) }
  val ring = if (isFocused) MonTheme.colors.borderFocus else Color.Transparent

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    textStyle =
      MonTheme.typography.bodyMedium.copy(
        color = MonTheme.colors.textPrimary,
        fontFamily = RobotoMono,
        lineHeight = 22.sp,
      ),
    cursorBrush = SolidColor(MonTheme.colors.secondary),
    modifier =
      modifier
        .onFocusChanged { isFocused = it.isFocused }
        .background(MonTheme.colors.primary, AREA_SHAPE)
        .border(2.dp, ring, AREA_SHAPE),
    decorationBox = { innerTextField ->
      // The padding is inside the field, so a tap anywhere in the area focuses it.
      Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp)) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            style = MonTheme.typography.bodyMedium.copy(color = MonTheme.colors.input.placeholder),
          )
        }
        innerTextField()
      }
    },
  )
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewTextArea() {
  MonTheme {
    Box(modifier = Modifier.padding(16.dp).height(160.dp)) {
      MonTextArea(
        value = "",
        onValueChange = {},
        placeholder = "Paste or type a text to read",
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}
