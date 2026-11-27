package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * The header of one reading's words in the Vocabulary tab: its title, how many words it has, and
 * "See all ›". The whole 32 dp row is the tap target.
 */
@Composable
fun VocabularyGroupHeader(
  title: String,
  countLabel: String,
  seeAllLabel: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier.fillMaxWidth().height(32.dp).clickable(role = Role.Button, onClick = onClick),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = title,
      style =
        MonTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MonTheme.colors.textPrimary,
        ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.widthIn(max = 200.dp),
    )
    Text(
      text = countLabel,
      style = MonTheme.typography.labelSmall.copy(color = MonTheme.colors.inkSoft),
      maxLines = 1,
    )
    Text(
      text = seeAllLabel,
      style = MonTheme.typography.labelSmall.copy(color = MonTheme.colors.secondary),
      modifier = Modifier.weight(1f),
      maxLines = 1,
      textAlign = androidx.compose.ui.text.style.TextAlign.End,
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun VocabularyGroupHeaderPreview() {
  MonTheme {
    VocabularyGroupHeader(
      title = "こころ",
      countLabel = "5 words",
      seeAllLabel = "See all ›",
      onClick = {},
      modifier = Modifier.padding(16.dp),
    )
  }
}
