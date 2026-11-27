package com.akrippner.mon.core.ui.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.SentenceRef

/**
 * A saved word on the vocabulary lists: the word with its kana and JLPT level, its gloss, the
 * sentence it was saved from with the word in bold, and the user's note. The whole card is one tap
 * target.
 */
@Composable
fun VocabWordCard(word: SavedWord, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val colors = MonTheme.colors
  val typography = MonTheme.typography
  val japanese = LocaleList("ja")

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(colors.listItem.background)
        .clickable(role = Role.Button, onClick = onClick)
        .padding(start = 18.dp, top = 8.dp, end = 16.dp, bottom = 10.dp)
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
      Text(
        text = word.dictionaryForm,
        style =
          typography.titleLarge.copy(
            fontWeight = FontWeight.Normal,
            color = colors.textPrimary,
            localeList = japanese,
          ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false),
      )
      Text(
        text = word.kana,
        style = typography.caption.copy(color = colors.secondary, localeList = japanese),
        maxLines = 1,
        modifier = Modifier.padding(bottom = 3.dp).weight(1f, fill = false),
      )
      Spacer(modifier = Modifier.weight(1f))
      word.jlpt?.let { JlptLevelChip(level = it, modifier = Modifier.padding(bottom = 5.dp)) }
    }
    if (word.gloss.isNotBlank()) {
      Text(
        text = word.gloss,
        style = typography.labelSmall.copy(color = colors.inkSoft),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 2.dp),
      )
    }
    SentenceWithWord(
      sentence = word.sentence,
      wordStyle = SpanStyle(fontWeight = FontWeight.Bold, color = colors.secondary),
      maxLines = 2,
      style = typography.caption.copy(color = colors.textPrimary, lineHeight = 16.sp),
      modifier = Modifier.padding(top = 6.dp),
    )
    word.note
      ?.takeIf { it.isNotBlank() }
      ?.let { note ->
        Text(
          text = "— $note",
          style = typography.labelSmall.copy(color = colors.secondary),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun VocabWordCardPreview() {
  MonTheme {
    VocabWordCard(
      word =
        SavedWord(
          id = "1",
          readingId = "r",
          dictionaryForm = "記憶",
          surface = "記憶",
          kana = "きおく",
          gloss = "memory; recollection",
          jlpt = null,
          jmdictSeq = null,
          sentence = SentenceRef("誰にもそう呼んだ記憶はない。", 8, 10, 0),
          note = "my note about memory themes",
          savedAt = 0,
        ),
      onClick = {},
      modifier = Modifier.padding(16.dp),
    )
  }
}
