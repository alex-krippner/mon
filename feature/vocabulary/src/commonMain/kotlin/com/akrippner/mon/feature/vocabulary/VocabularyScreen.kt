package com.akrippner.mon.feature.vocabulary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.component.JlptFilterRow
import com.akrippner.mon.core.designsystem.component.MonSearchBar
import com.akrippner.mon.core.designsystem.component.MonSnackbarHost
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.VocabularyGroupHeader
import com.akrippner.mon.core.designsystem.component.showUndo
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.ui.vocabulary.VocabWordCard
import com.akrippner.mon.feature.vocabulary.generated.resources.Res
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_back
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_empty_body
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_empty_title
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_error_load_failed
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_filter_all
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_group_count
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_more_from
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_no_matches
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_search_placeholder
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_see_all
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_snackbar_removed
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_snackbar_undo
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_subtitle
import com.akrippner.mon.feature.vocabulary.generated.resources.vocabulary_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Shows the snackbar for a one-off [effect] of this screen, for the host that collects the effects.
 * Returns false if the effect is not about a snackbar. Waits until the snackbar is gone, so call it
 * in its own coroutine.
 */
suspend fun SnackbarHostState.showVocabularyEffect(
  effect: VocabularyEffect,
  onIntent: (VocabularyIntent) -> Unit,
): Boolean {
  if (effect !is VocabularyEffect.ShowRemoved) return false
  if (
    showUndo(
      message = getString(Res.string.vocabulary_snackbar_removed),
      actionLabel = getString(Res.string.vocabulary_snackbar_undo),
    )
  ) {
    onIntent(VocabularyIntent.UndoRemove(effect.word))
  }
  return true
}

/**
 * The Vocabulary tab, or, when [VocabularyState.forOneReading], the list of one reading's saved
 * words.
 */
@Composable
fun VocabularyScreen(
  state: VocabularyState,
  onIntent: (VocabularyIntent) -> Unit,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  Scaffold(
    title = stringResource(Res.string.vocabulary_title),
    subtitle =
      if (state.forOneReading) {
        stringResource(Res.string.vocabulary_subtitle, state.readingTitle, state.totalWords)
      } else {
        null
      },
    navigationIcon = if (state.forOneReading) Icon.Core.ChevronLeft else null,
    navigationDescription = stringResource(Res.string.vocabulary_back),
    onNavigationClick = { onIntent(VocabularyIntent.Back) },
    snackbarHost = { MonSnackbarHost(snackbarHostState) },
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      when {
        state.isLoading ->
          CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = MonTheme.colors.secondary,
          )
        state.error != null ->
          CenteredMessage(stringResource(Res.string.vocabulary_error_load_failed))
        state.isEmpty ->
          CenteredMessage(
            title = stringResource(Res.string.vocabulary_empty_title),
            text = stringResource(Res.string.vocabulary_empty_body),
          )
        else -> VocabularyList(state, onIntent)
      }
    }
  }

  state.openWord?.let { SavedWordSheet(open = it, onIntent = onIntent) }
}

@Composable
private fun VocabularyList(state: VocabularyState, onIntent: (VocabularyIntent) -> Unit) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding =
      PaddingValues(
        start = MonTheme.space.screenMargin,
        end = MonTheme.space.screenMargin,
        top = MonTheme.space.spacing4,
        bottom = MonTheme.space.spacing4,
      ),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    item(key = "search") {
      MonSearchBar(
        query = state.query,
        onQueryChange = { onIntent(VocabularyIntent.QueryChanged(it)) },
        placeholder = stringResource(Res.string.vocabulary_search_placeholder),
        startIcon = {
          MonIcon(
            id = Icon.Core.Search,
            primaryColor = MonTheme.colors.input.placeholder,
            size = IconSize.ExtraSmall,
          )
        },
        modifier = Modifier.fillMaxWidth(),
      )
    }
    if (state.showLevelFilter) {
      item(key = "levels") {
        JlptFilterRow(
          selected = state.levels.mapTo(HashSet()) { it.name },
          onToggle = { name -> onIntent(VocabularyIntent.LevelToggled(JlptLevel.valueOf(name))) },
          onClear = { onIntent(VocabularyIntent.ClearLevels) },
          allLabel = stringResource(Res.string.vocabulary_filter_all),
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
    if (state.hasNoMatches) {
      item(key = "no-matches") {
        Text(
          text = stringResource(Res.string.vocabulary_no_matches),
          style = MonTheme.typography.bodyMedium,
          color = MonTheme.colors.inkSoft,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        )
      }
    }
    state.sections.forEach { section ->
      // The list of one reading has no header: its title is in the bar.
      if (!state.forOneReading) {
        item(key = "header-${section.readingId}") {
          VocabularyGroupHeader(
            title = section.title,
            countLabel = stringResource(Res.string.vocabulary_group_count, section.wordCount),
            seeAllLabel = stringResource(Res.string.vocabulary_see_all),
            onClick = { onIntent(VocabularyIntent.OpenReading(section.readingId)) },
          )
        }
      }
      items(section.words, key = { it.id }) { word ->
        VocabWordCard(word = word, onClick = { onIntent(VocabularyIntent.WordTapped(word.id)) })
      }
      if (section.moreCount > 0) {
        item(key = "more-${section.readingId}") {
          Text(
            text =
              stringResource(Res.string.vocabulary_more_from, section.moreCount, section.title),
            style = MonTheme.typography.labelSmall.copy(color = MonTheme.colors.secondary),
            modifier =
              Modifier.fillMaxWidth()
                .clickable(role = Role.Button) {
                  onIntent(VocabularyIntent.OpenReading(section.readingId))
                }
                .padding(vertical = 12.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun BoxScope.CenteredMessage(text: String, title: String? = null) {
  Column(
    modifier = Modifier.align(Alignment.Center).padding(MonTheme.space.screenMargin),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (title != null) {
      Text(
        text = title,
        style = MonTheme.typography.titleMedium,
        color = MonTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
      )
    }
    Text(
      text = text,
      style = MonTheme.typography.bodyMedium,
      color = MonTheme.colors.textPrimary,
      textAlign = TextAlign.Center,
    )
  }
}
