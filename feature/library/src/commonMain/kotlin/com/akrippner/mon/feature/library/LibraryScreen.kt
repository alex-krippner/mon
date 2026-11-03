package com.akrippner.mon.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.akrippner.mon.core.designsystem.bottomBorder
import com.akrippner.mon.core.designsystem.component.FabPosition
import com.akrippner.mon.core.designsystem.component.FloatingActionButton
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.TextListItem
import com.akrippner.mon.core.designsystem.conditional
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.feature.library.generated.resources.Res
import com.akrippner.mon.feature.library.generated.resources.library_all_texts
import com.akrippner.mon.feature.library.generated.resources.library_error_load_failed
import com.akrippner.mon.feature.library.generated.resources.library_title
import com.akrippner.mon.feature.library.generated.resources.library_word_count
import org.jetbrains.compose.resources.stringResource

@Composable
fun LibraryScreen(state: LibraryState, onIntent: (LibraryIntent) -> Unit) {
  val listState = rememberLazyListState()
  val isScrolled by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
    }
  }

  Scaffold(
    title = stringResource(Res.string.library_title),
    floatingActionButtonPosition = FabPosition.END,
    floatingActionButton = {
      FloatingActionButton(
        onClick = { onIntent(LibraryIntent.AddReading) },
        modifier = Modifier.offset(-MonTheme.space.spacing10),
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(MonTheme.space.spacing10),
      verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing5),
    ) {
      Text(
        text = stringResource(Res.string.library_all_texts),
        style = MonTheme.typography.titleMedium,
        color = MonTheme.colors.textPrimary,
        modifier =
          Modifier.fillMaxWidth()
            .conditional(isScrolled) {
              bottomBorder(
                MonTheme.space.spacing2px,
                color = MonTheme.colors.secondary.copy(alpha = 0.1f),
              )
            }
            .padding(bottom = MonTheme.space.spacing5),
      )
      state.error?.let { error ->
        Text(
          text = error.message(),
          style = MonTheme.typography.bodyMedium,
          color = MonTheme.colors.textPrimary,
        )
      }
      LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing3),
        contentPadding = PaddingValues(bottom = MonTheme.space.spacing4),
        modifier = Modifier.weight(1f).padding(bottom = MonTheme.space.spacing10),
      ) {
        items(state.readings, key = { it.id }) { reading ->
          TextListItem(
            title = reading.title,
            meta = stringResource(Res.string.library_word_count, reading.tokenCount),
            onClick = { onIntent(LibraryIntent.OpenReading(reading.id)) },
          )
        }
      }
    }
  }
}

@Composable
private fun LibraryError.message(): String =
  when (this) {
    LibraryError.LoadFailed -> stringResource(Res.string.library_error_load_failed)
  }
