package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * The Reader's vocabulary button: a 19 dp pill with a bookmark and the number of saved words. It is
 * [active] while the Reader was opened from the vocabulary list. Its touch target is still at least
 * 48 dp, so a caller that aligns the pill must make up the difference.
 *
 * @param description What a screen reader says, e.g. "Vocabulary for this reading, 5 words".
 */
@Composable
fun ReaderVocabularyButton(
  count: Int,
  active: Boolean,
  onClick: () -> Unit,
  description: String,
  modifier: Modifier = Modifier,
) {
  val colors = MonTheme.colors
  val background = if (active) colors.tokenizedText.background else colors.primary
  val content = if (active) colors.secondary else colors.textPrimary
  Row(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .height(19.dp)
        .clip(CircleShape)
        .background(background)
        .semantics { contentDescription = description }
        .clickable(role = Role.Button, onClick = onClick)
        .padding(start = 5.dp, end = 7.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    MonIcon(id = Icon.Core.BookmarkFilled, primaryColor = content, requiredSize = 12.dp)
    Text(
      text = count.toString(),
      style = MonTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = content),
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFFDFAF4)
@Composable
private fun ReaderVocabularyButtonPreview() {
  MonTheme {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      ReaderVocabularyButton(5, active = false, onClick = {}, description = "5 words")
      ReaderVocabularyButton(5, active = true, onClick = {}, description = "5 words")
    }
  }
}
