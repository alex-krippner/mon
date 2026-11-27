package com.akrippner.mon.core.ui.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.model.data.MAX_NOTE_LENGTH

/**
 * The "Note" label and field of the look-up sheet and the saved-word sheet: a single line, 40 dp
 * high, on the sheet. [onCommit] is called when the field loses focus; the caller also commits when
 * the sheet closes.
 */
@Composable
fun SheetNoteField(
  value: String,
  onValueChange: (String) -> Unit,
  onCommit: () -> Unit,
  label: String,
  placeholder: String,
  modifier: Modifier = Modifier,
) {
  val colors = MonTheme.colors
  var hadFocus by remember { mutableStateOf(false) }
  Column(modifier = modifier) {
    Text(text = label, style = MonTheme.typography.labelSmall.copy(color = colors.sheetLabel))
    BasicTextField(
      value = value,
      onValueChange = { onValueChange(it.take(MAX_NOTE_LENGTH)) },
      singleLine = true,
      textStyle = MonTheme.typography.labelMedium.copy(color = colors.lookup.title),
      cursorBrush = SolidColor(colors.lookup.title),
      modifier =
        Modifier.padding(top = 6.dp)
          .fillMaxWidth()
          .height(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(colors.sheetField)
          .border(1.dp, colors.sheetFieldBorder, RoundedCornerShape(10.dp))
          .onFocusChanged {
            if (hadFocus && !it.isFocused) onCommit()
            hadFocus = it.isFocused
          },
      decorationBox = { innerTextField ->
        Box(
          modifier = Modifier.padding(horizontal = 12.dp),
          contentAlignment = Alignment.CenterStart,
        ) {
          if (value.isEmpty()) {
            Text(
              text = placeholder,
              style = MonTheme.typography.labelMedium.copy(color = colors.sheetLabel),
            )
          }
          innerTextField()
        }
      },
    )
  }
}
