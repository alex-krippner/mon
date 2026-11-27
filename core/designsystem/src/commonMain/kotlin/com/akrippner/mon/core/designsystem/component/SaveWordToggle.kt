package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * The Save toggle on the look-up sheet: a 32 dp pill that is an outline while the word is not saved
 * and vermilion once it is. Its touch target is still at least 48 dp.
 *
 * @param description What a screen reader says, e.g. "Save 先生 to vocabulary".
 */
@Composable
fun SaveWordToggle(
  saved: Boolean,
  onToggle: () -> Unit,
  savedLabel: String,
  unsavedLabel: String,
  description: String,
  modifier: Modifier = Modifier,
) {
  val colors = MonTheme.colors
  val content = colors.lookup.title // onSheet
  Surface(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .height(32.dp)
        .semantics { contentDescription = description }
        .toggleable(value = saved, role = Role.Switch, onValueChange = { onToggle() }),
    shape = CircleShape,
    color = if (saved) colors.buttonPrimary.background else Color.Transparent,
    contentColor = content,
    border = if (saved) null else BorderStroke(1.dp, content),
  ) {
    Row(
      modifier = Modifier.padding(start = 9.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      MonIcon(
        id = if (saved) Icon.Core.BookmarkFilled else Icon.Core.Bookmark,
        primaryColor = content,
        requiredSize = 16.dp,
      )
      Text(
        text = if (saved) savedLabel else unsavedLabel,
        style =
          MonTheme.typography.labelMedium.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
          ),
      )
    }
  }
}

@Preview
@Composable
private fun SaveWordTogglePreview() {
  MonTheme {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      SaveWordToggle(false, {}, "Saved", "Save", "Save 先生 to vocabulary")
      SaveWordToggle(true, {}, "Saved", "Save", "Save 先生 to vocabulary")
    }
  }
}
