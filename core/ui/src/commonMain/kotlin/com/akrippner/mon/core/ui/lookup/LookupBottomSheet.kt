package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.component.SaveWordToggle
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.ui.DisplayToken
import com.akrippner.mon.core.ui.JmdictEntry
import com.akrippner.mon.core.ui.vocabulary.SheetNoteField

/**
 * The bottom sheet that shows what a dictionary says about a tapped word.
 *
 * The Save toggle and the note field only appear when [save] is given, and the kanji link when
 * [onOpenKanji] is, so a screen that cannot do these simply leaves them out. The note is reported
 * with [onNoteChange] as it is typed, and [onNoteCommit] when the field loses focus; the screen
 * also commits it when the sheet closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookupBottomSheet(
  token: DisplayToken,
  lookupState: LookupUiState,
  strings: LookupStrings,
  onRetry: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  onOpenDictionaries: (() -> Unit)? = null,
  save: LookupSave? = null,
  onToggleSave: () -> Unit = {},
  onNoteChange: (String) -> Unit = {},
  onNoteCommit: () -> Unit = {},
  onOpenKanji: (() -> Unit)? = null,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    // The sheet is always fully open (the design has no half-open state), so it never rests partly.
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = MonTheme.colors.lookup.background,
    contentColor = Color.White,
    // The design: a 10% black scrim and a square top edge.
    scrimColor = Color.Black.copy(alpha = SCRIM_ALPHA),
    shape = RectangleShape,
    dragHandle = {
      // The 60×4 dp white handle of the design.
      Box(
        modifier =
          Modifier.padding(top = 12.dp, bottom = 8.dp)
            .size(width = 60.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White)
      )
    },
    modifier = modifier,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          // The design's sheet is 465 dp high, handle included, however little it shows.
          .heightIn(min = SHEET_HEIGHT - HANDLE_HEIGHT)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = MonTheme.space.screenMargin)
          .padding(top = 12.dp, bottom = 32.dp)
          .navigationBarsPadding()
    ) {
      when (lookupState) {
        LookupUiState.Loading -> LookupLoadingContent()
        is LookupUiState.NotFound ->
          LookupMessageContent(surface = token.surface, message = strings.notFound)
        LookupUiState.NoDictionaryInstalled ->
          LookupMessageContent(
            surface = token.surface,
            message = strings.noDictionary,
            actionLabel = strings.openDictionaries.takeIf { onOpenDictionaries != null },
            onAction = onOpenDictionaries,
          )
        LookupUiState.Unavailable ->
          LookupMessageContent(
            surface = token.surface,
            message = strings.unavailable,
            actionLabel = strings.retry,
            onAction = onRetry,
          )
        is LookupUiState.Found ->
          LookupFoundContent(
            entries = lookupState.entries,
            strings = strings,
            save = save,
            onToggleSave = onToggleSave,
            onNoteChange = onNoteChange,
            onNoteCommit = onNoteCommit,
            onOpenKanji = onOpenKanji,
          )
      }
    }
  }
}

private const val SCRIM_ALPHA = 0.10f
private val SHEET_HEIGHT = 465.dp

/** The drag handle with its padding above and below (12 + 4 + 8 dp). */
private val HANDLE_HEIGHT = 24.dp

@Composable
private fun LookupLoadingContent() {
  Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(color = MonTheme.colors.secondary)
  }
}

/** A centered message about [surface], with an optional action button below it. */
@Composable
private fun LookupMessageContent(
  surface: String,
  message: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = surface,
        style = MonTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.40f)),
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = message,
        style = MonTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.45f)),
        textAlign = TextAlign.Center,
      )
      if (actionLabel != null && onAction != null) {
        Spacer(Modifier.height(12.dp))
        YomuPillButton(label = actionLabel, variant = PillVariant.Outline, onClick = onAction)
      }
    }
  }
}

@Composable
private fun LookupFoundContent(
  entries: List<JmdictEntry>,
  strings: LookupStrings,
  save: LookupSave?,
  onToggleSave: () -> Unit,
  onNoteChange: (String) -> Unit,
  onNoteCommit: () -> Unit,
  onOpenKanji: (() -> Unit)?,
) {
  entries.forEachIndexed { index, entry ->
    if (index > 0) {
      Spacer(Modifier.height(16.dp))
      HorizontalDivider(color = Color.White.copy(alpha = 0.18f), thickness = 1.dp)
      Spacer(Modifier.height(16.dp))
    }
    // The toggle saves the tapped word, described by the first entry, so only that entry has it.
    val toggle: (@Composable () -> Unit)? =
      if (index == 0 && save != null) {
        {
          SaveWordToggle(
            saved = save.saved,
            onToggle = onToggleSave,
            savedLabel = strings.saved,
            unsavedLabel = strings.save,
            description = save.description,
          )
        }
      } else {
        null
      }
    LookupEntryContent(entry, trailing = toggle)
  }

  if (save != null && save.saved) {
    Spacer(Modifier.height(22.dp))
    SheetNoteField(
      value = save.note,
      onValueChange = onNoteChange,
      onCommit = onNoteCommit,
      label = strings.noteLabel,
      placeholder = strings.notePlaceholder,
    )
    Text(
      text = save.helper,
      style = MonTheme.typography.labelSmall.copy(color = MonTheme.colors.sheetLabel),
      modifier = Modifier.padding(top = 10.dp),
    )
  }
  if (onOpenKanji != null) {
    Spacer(Modifier.height(12.dp))
    KanjiDetailLink(onClick = onOpenKanji)
  }
}

@Composable
private fun LookupEntryContent(entry: JmdictEntry, trailing: (@Composable () -> Unit)? = null) {
  WordDisplay(
    surface = entry.surface,
    reading = entry.reading,
    jlptLevel = entry.jlptLevel,
    trailing = trailing,
  )

  entry.senses.forEachIndexed { index, sense ->
    Spacer(Modifier.height(24.dp))
    SenseBlock(index = index + 1, sense = sense)
  }
}
