package com.akrippner.mon.feature.reader

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.NoticeTone
import com.akrippner.mon.core.designsystem.component.FuriganaToggle
import com.akrippner.mon.core.designsystem.component.MonNotice
import com.akrippner.mon.core.designsystem.component.MonSnackbarHost
import com.akrippner.mon.core.designsystem.component.ReaderVocabularyButton
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.showUndo
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.ui.TokenizedText
import com.akrippner.mon.core.ui.lookup.LookupBottomSheet
import com.akrippner.mon.core.ui.lookup.LookupSave
import com.akrippner.mon.core.ui.lookup.LookupStrings
import com.akrippner.mon.feature.reader.generated.resources.Res
import com.akrippner.mon.feature.reader.generated.resources.reader_back_to_vocabulary
import com.akrippner.mon.feature.reader.generated.resources.reader_empty
import com.akrippner.mon.feature.reader.generated.resources.reader_error_load_failed
import com.akrippner.mon.feature.reader.generated.resources.reader_error_not_found
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_no_dictionary
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_not_found
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_note_label
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_note_placeholder
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_open_dictionaries
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_retry
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_save
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_save_description
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_saved
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_saved_description
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_saved_helper
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_unavailable
import com.akrippner.mon.feature.reader.generated.resources.reader_pending_body
import com.akrippner.mon.feature.reader.generated.resources.reader_pending_link
import com.akrippner.mon.feature.reader.generated.resources.reader_pending_title
import com.akrippner.mon.feature.reader.generated.resources.reader_saved_words
import com.akrippner.mon.feature.reader.generated.resources.reader_snackbar_removed
import com.akrippner.mon.feature.reader.generated.resources.reader_snackbar_save_failed
import com.akrippner.mon.feature.reader.generated.resources.reader_snackbar_undo
import com.akrippner.mon.feature.reader.generated.resources.reader_unsupported_body
import com.akrippner.mon.feature.reader.generated.resources.reader_unsupported_title
import com.akrippner.mon.feature.reader.generated.resources.reader_vocabulary_description
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Shows the snackbar for a one-off [effect] of the reader, for the host that collects the effects.
 * Returns false if the effect is not about a snackbar. Waits until the snackbar is gone, so call it
 * in its own coroutine.
 */
suspend fun SnackbarHostState.showReaderEffect(
  effect: ReaderEffect,
  onIntent: (ReaderIntent) -> Unit,
): Boolean {
  when (effect) {
    is ReaderEffect.ShowRemoved ->
      if (
        showUndo(
          message = getString(Res.string.reader_snackbar_removed),
          actionLabel = getString(Res.string.reader_snackbar_undo),
        )
      ) {
        onIntent(ReaderIntent.UndoRemove(effect.word))
      }
    ReaderEffect.SaveFailed -> showSnackbar(getString(Res.string.reader_snackbar_save_failed))
    else -> return false
  }
  return true
}

@Composable
fun ReaderScreen(
  state: ReaderState,
  onIntent: (ReaderIntent) -> Unit,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  Scaffold(
    title = state.title,
    navigationIcon = Icon.Core.ChevronLeft,
    onNavigationClick = { onIntent(ReaderIntent.Back) },
    navigationDescription =
      if (state.openedFromVocabulary) stringResource(Res.string.reader_back_to_vocabulary)
      else null,
    snackbarHost = { MonSnackbarHost(snackbarHostState) },
    topBarActions = {
      if (state.savedCount > 0) {
        ReaderVocabularyButton(
          count = state.savedCount,
          active = state.openedFromVocabulary,
          onClick = { onIntent(ReaderIntent.OpenVocabulary) },
          description = stringResource(Res.string.reader_vocabulary_description, state.savedCount),
        )
        Spacer(Modifier.width(8.dp))
      }
      // Only tokens have furigana, so the toggle means nothing for a pending reading.
      if (state.paragraphs.isNotEmpty()) {
        FuriganaToggle(
          enabled = state.showFurigana,
          onToggle = { onIntent(ReaderIntent.ToggleFurigana) },
        )
        // The design keeps the pill 16 dp from the screen edge. The toggle's 48 dp touch target is
        // 7.5 dp wider than the pill on each side.
        Spacer(Modifier.width(8.5.dp))
      }
    },
    // The scaffold has already applied the bar insets to the content.
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      when {
        state.isLoading ->
          CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = MonTheme.colors.secondary,
          )
        state.error != null ->
          Text(
            text = state.error.message(),
            modifier = Modifier.align(Alignment.Center).padding(MonTheme.space.screenMargin),
            style = MonTheme.typography.bodyMedium,
            color = MonTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
          )
        // A tokenized reading can still have nothing to draw, if its text is only whitespace.
        !state.isPending && state.paragraphs.isEmpty() ->
          Text(
            text = stringResource(Res.string.reader_empty),
            modifier = Modifier.align(Alignment.Center).padding(MonTheme.space.screenMargin),
            style = MonTheme.typography.bodyMedium,
            color = MonTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
          )
        else -> ReaderContent(state, onIntent)
      }
    }
  }

  state.selection?.let { selection ->
    val saved = selection.saved
    LookupBottomSheet(
      token = selection.token,
      lookupState = selection.lookup,
      strings = lookupStrings(),
      onRetry = { onIntent(ReaderIntent.RetryLookup) },
      onDismiss = { onIntent(ReaderIntent.DismissLookup) },
      onOpenDictionaries = { onIntent(ReaderIntent.OpenDictionaries) },
      save =
        if (selection.canSave) {
          LookupSave(
            saved = saved != null,
            note = selection.noteDraft,
            description =
              if (saved != null) {
                stringResource(Res.string.reader_lookup_saved_description, selection.token.surface)
              } else {
                stringResource(Res.string.reader_lookup_save_description, selection.token.surface)
              },
            helper = stringResource(Res.string.reader_lookup_saved_helper, state.title),
          )
        } else {
          null
        },
      onToggleSave = { onIntent(ReaderIntent.ToggleSave) },
      onNoteChange = { onIntent(ReaderIntent.NoteChanged(it)) },
      onNoteCommit = { onIntent(ReaderIntent.NoteCommitted) },
    )
  }
}

@Composable
private fun ReaderContent(state: ReaderState, onIntent: (ReaderIntent) -> Unit) {
  val listState = rememberLazyListState()
  val focus = state.focus
  var outlinedBounds by remember { mutableStateOf<Rect?>(null) }
  // Saved, so a rotation does not scroll back to the word after the user has read on.
  var focusShown by rememberSaveable { mutableStateOf(false) }

  // "Show in reading": bring the word's paragraph into view, then put the word in the top third of
  // the screen. The paragraph reports where the word is once it is laid out.
  LaunchedEffect(focus?.tokenIndex) {
    if (focus == null || focusShown) return@LaunchedEffect
    val paragraph =
      state.paragraphs.indexOfFirst { focus.tokenIndex in it.first().index..it.last().index }
    if (paragraph < 0) return@LaunchedEffect
    listState.scrollToItem(paragraph)
    val bounds = snapshotFlow { outlinedBounds }.filterNotNull().first()
    listState.scrollBy(bounds.top - listState.layoutInfo.viewportSize.height / 3f)
    focusShown = true
  }
  // The outline points the eye at the word; the first scroll by the user ends it.
  LaunchedEffect(focusShown, focus?.outlined) {
    if (!focusShown || focus?.outlined != true) return@LaunchedEffect
    snapshotFlow { listState.isScrollInProgress }.first { it }
    onIntent(ReaderIntent.ClearOutline)
  }

  // Lazy, so a long text composes only the paragraphs on screen.
  LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
    // The design leaves some air between the top bar and the first line.
    contentPadding =
      PaddingValues(
        start = MonTheme.space.screenMargin,
        end = MonTheme.space.screenMargin,
        top = MonTheme.space.spacing8,
        bottom = MonTheme.space.spacing4,
      ),
    verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing5),
  ) {
    if (state.isPending) {
      item(key = "pending-notice") {
        // Only a language with a tokenizer pack can leave this state, so only it gets the promise
        // and the link to Dictionaries.
        if (state.tokenizerMayArrive) {
          MonNotice(
            title = stringResource(Res.string.reader_pending_title),
            body = stringResource(Res.string.reader_pending_body),
            tone = NoticeTone.Info,
            linkLabel = stringResource(Res.string.reader_pending_link),
            onLinkClick = { onIntent(ReaderIntent.OpenDictionaries) },
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          MonNotice(
            title = stringResource(Res.string.reader_unsupported_title),
            body = stringResource(Res.string.reader_unsupported_body),
            tone = NoticeTone.Info,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
      item(key = "pending-text") {
        Text(
          text = state.plainText,
          style = MonTheme.typography.bodyMedium,
          color = MonTheme.colors.textPrimary,
        )
      }
    }
    itemsIndexed(state.paragraphs, key = { index, _ -> index }) { _, paragraph ->
      // Only the paragraph that holds the selected token gets its index, so a tap recomposes one
      // paragraph and not the whole text.
      val selectedIndex = state.selection?.index
      val outlinedIndex = focus?.takeIf { it.outlined }?.tokenIndex
      val range = paragraph.first().index..paragraph.last().index
      ReaderParagraph(
        paragraph = paragraph,
        selectedIndex = selectedIndex?.takeIf { it in range },
        outlinedIndex = outlinedIndex?.takeIf { it in range },
        onOutlinedBounds = { outlinedBounds = it },
        showFurigana = state.showFurigana,
        onIntent = onIntent,
      )
    }
  }
}

@Composable
private fun ReaderParagraph(
  paragraph: List<ReaderToken>,
  selectedIndex: Int?,
  outlinedIndex: Int?,
  onOutlinedBounds: (Rect) -> Unit,
  showFurigana: Boolean,
  onIntent: (ReaderIntent) -> Unit,
) {
  // The text works with positions in the paragraph; the view model with positions in the reading.
  val tokens = remember(paragraph) { paragraph.map { it.display } }
  TokenizedText(
    tokens = tokens,
    showFurigana = showFurigana,
    selectedIndex =
      selectedIndex?.let { selected -> paragraph.indexOfFirst { it.index == selected } },
    outlinedIndex =
      outlinedIndex?.let { outlined -> paragraph.indexOfFirst { it.index == outlined } },
    onOutlinedBounds = onOutlinedBounds,
    onTokenTap = { position -> onIntent(ReaderIntent.TokenTapped(paragraph[position].index)) },
    modifier = Modifier.fillMaxWidth(),
    savedLabel = stringResource(Res.string.reader_saved_words),
  )
}

@Composable
private fun lookupStrings() =
  LookupStrings(
    notFound = stringResource(Res.string.reader_lookup_not_found),
    unavailable = stringResource(Res.string.reader_lookup_unavailable),
    noDictionary = stringResource(Res.string.reader_lookup_no_dictionary),
    openDictionaries = stringResource(Res.string.reader_lookup_open_dictionaries),
    retry = stringResource(Res.string.reader_lookup_retry),
    save = stringResource(Res.string.reader_lookup_save),
    saved = stringResource(Res.string.reader_lookup_saved),
    noteLabel = stringResource(Res.string.reader_lookup_note_label),
    notePlaceholder = stringResource(Res.string.reader_lookup_note_placeholder),
  )

@Composable
private fun ReaderError.message(): String =
  when (this) {
    ReaderError.NotFound -> stringResource(Res.string.reader_error_not_found)
    ReaderError.LoadFailed -> stringResource(Res.string.reader_error_load_failed)
  }
