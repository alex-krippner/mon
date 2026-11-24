package com.akrippner.mon.feature.reader

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.NoticeTone
import com.akrippner.mon.core.designsystem.component.FuriganaToggle
import com.akrippner.mon.core.designsystem.component.MonNotice
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.ui.TokenizedText
import com.akrippner.mon.core.ui.lookup.LookupBottomSheet
import com.akrippner.mon.core.ui.lookup.LookupStrings
import com.akrippner.mon.feature.reader.generated.resources.Res
import com.akrippner.mon.feature.reader.generated.resources.reader_empty
import com.akrippner.mon.feature.reader.generated.resources.reader_error_load_failed
import com.akrippner.mon.feature.reader.generated.resources.reader_error_not_found
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_no_dictionary
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_not_found
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_open_dictionaries
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_retry
import com.akrippner.mon.feature.reader.generated.resources.reader_lookup_unavailable
import com.akrippner.mon.feature.reader.generated.resources.reader_pending_body
import com.akrippner.mon.feature.reader.generated.resources.reader_pending_link
import com.akrippner.mon.feature.reader.generated.resources.reader_pending_title
import com.akrippner.mon.feature.reader.generated.resources.reader_unsupported_body
import com.akrippner.mon.feature.reader.generated.resources.reader_unsupported_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReaderScreen(state: ReaderState, onIntent: (ReaderIntent) -> Unit) {
  Scaffold(
    title = state.title,
    navigationIcon = Icon.Core.ChevronLeft,
    onNavigationClick = { onIntent(ReaderIntent.Back) },
    topBarActions = {
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
    LookupBottomSheet(
      token = selection.token,
      lookupState = selection.lookup,
      strings = lookupStrings(),
      onRetry = { onIntent(ReaderIntent.RetryLookup) },
      onDismiss = { onIntent(ReaderIntent.DismissLookup) },
      onOpenDictionaries = { onIntent(ReaderIntent.OpenDictionaries) },
    )
  }
}

@Composable
private fun ReaderContent(state: ReaderState, onIntent: (ReaderIntent) -> Unit) {
  // Lazy, so a long text composes only the paragraphs on screen.
  LazyColumn(
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
      ReaderParagraph(
        paragraph = paragraph,
        selectedIndex =
          selectedIndex?.takeIf { it in paragraph.first().index..paragraph.last().index },
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
    onTokenTap = { position -> onIntent(ReaderIntent.TokenTapped(paragraph[position].index)) },
    modifier = Modifier.fillMaxWidth(),
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
  )

@Composable
private fun ReaderError.message(): String =
  when (this) {
    ReaderError.NotFound -> stringResource(Res.string.reader_error_not_found)
    ReaderError.LoadFailed -> stringResource(Res.string.reader_error_load_failed)
  }
