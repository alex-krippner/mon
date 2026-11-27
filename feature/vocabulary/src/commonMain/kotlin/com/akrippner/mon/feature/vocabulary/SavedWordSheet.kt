package com.akrippner.mon.feature.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.PillSize
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.ui.vocabulary.JlptChipSize
import com.akrippner.mon.core.ui.vocabulary.JlptLevelChip
import com.akrippner.mon.core.ui.vocabulary.SentenceWithWord
import com.akrippner.mon.core.ui.vocabulary.SheetNoteField
import com.akrippner.mon.feature.vocabulary.generated.resources.Res
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_sheet_note_label
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_sheet_note_placeholder
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_sheet_remove
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_sheet_saved_from
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_sheet_show_in_reading
import org.jetbrains.compose.resources.stringResource

/**
 * The sheet of a saved word: its entry as it was saved, the sentence it came from, an editable
 * note, and the two actions, "Show in reading" and "Remove from vocabulary".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedWordSheet(open: OpenWord, onIntent: (VocabularyIntent) -> Unit) {
  val colors = MonTheme.colors
  val word = open.word
  ModalBottomSheet(
    onDismissRequest = { onIntent(VocabularyIntent.DismissWord) },
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = colors.lookup.background,
    contentColor = Color.White,
    // This sheet covers a list the user can still see: a darker scrim than the look-up sheet's.
    scrimColor = colors.scrimStrong,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    dragHandle = {
      Box(
        modifier =
          Modifier.padding(top = 12.dp, bottom = 8.dp)
            .size(width = 60.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White)
      )
    },
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = MonTheme.space.screenMargin)
          .padding(top = 12.dp, bottom = 39.dp)
          .navigationBarsPadding()
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = word.dictionaryForm,
          style = MonTheme.typography.displaySmall,
          color = colors.lookup.title,
        )
        word.jlpt?.let { JlptLevelChip(level = it, size = JlptChipSize.Sheet) }
      }
      Text(
        text = word.kana,
        style = MonTheme.typography.bodyMedium.copy(localeList = LocaleList("ja")),
        color = colors.sheetLabel,
        modifier = Modifier.padding(top = 4.dp),
      )
      if (word.gloss.isNotBlank()) {
        Text(
          text = word.gloss,
          style = MonTheme.typography.bodyMedium,
          color = colors.lookup.title,
          modifier = Modifier.padding(top = 12.dp),
        )
      }

      Text(
        text = stringResource(Res.string.vocabulary_sheet_saved_from, open.readingTitle),
        style = MonTheme.typography.labelSmall.copy(color = colors.sheetLabel),
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
      )
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.sheetField)
            .padding(12.dp)
      ) {
        SentenceWithWord(
          sentence = word.sentence,
          wordStyle = SpanStyle(fontWeight = FontWeight.Bold, color = colors.sheetAccentText),
          maxLines = 4,
          style = MonTheme.typography.bodyMedium.copy(color = colors.lookup.title),
        )
      }

      SheetNoteField(
        value = open.noteDraft,
        onValueChange = { onIntent(VocabularyIntent.NoteChanged(it)) },
        onCommit = { onIntent(VocabularyIntent.NoteCommitted) },
        label = stringResource(Res.string.vocabulary_sheet_note_label),
        placeholder = stringResource(Res.string.vocabulary_sheet_note_placeholder),
        modifier = Modifier.padding(top = 20.dp),
      )

      YomuPillButton(
        label = stringResource(Res.string.vocabulary_sheet_show_in_reading),
        variant = PillVariant.Filled,
        size = PillSize.Large,
        onClick = { onIntent(VocabularyIntent.ShowInReading) },
        modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = stringResource(Res.string.vocabulary_sheet_remove),
        style = MonTheme.typography.labelButton.copy(color = colors.sheetAccentText),
        modifier =
          Modifier.align(Alignment.CenterHorizontally).minimumInteractiveComponentSize().clickable(
            role = Role.Button
          ) {
            onIntent(VocabularyIntent.Remove)
          },
      )
    }
  }
}
