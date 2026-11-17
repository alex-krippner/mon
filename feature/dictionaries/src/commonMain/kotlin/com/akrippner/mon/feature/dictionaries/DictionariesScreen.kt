package com.akrippner.mon.feature.dictionaries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.NoticeTone
import com.akrippner.mon.core.designsystem.PillSize
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.component.MonNotice
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.dashedBorder
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.dictionary.pack.CatalogError
import com.akrippner.mon.core.dictionary.pack.Pack
import com.akrippner.mon.core.dictionary.pack.PackError
import com.akrippner.mon.core.dictionary.pack.PackState
import com.akrippner.mon.feature.dictionaries.generated.resources.Res
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_check_again
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_check_failed_empty_invalid
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_check_failed_empty_network
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_check_failed_empty_title
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_check_failed_kept
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_checking
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_empty
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_empty_body
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_for_pack
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_intro_downloading
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_intro_first_run
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_intro_update
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_intro_updates
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_ready_body
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_ready_link
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_ready_title
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_section_available
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_section_installed
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_title
import org.jetbrains.compose.resources.stringResource

/** Up to six tile columns on a wide window: 6 × 137 dp plus the 8 dp gaps. */
private val MAX_CONTENT_WIDTH = 862.dp

/** Text stays readable on a wide window. */
private val MAX_TEXT_WIDTH = 427.dp

/** Mon stands on a downloading tile's top edge, so tiles keep this much free space above. */
private val ROOM_ABOVE_TILE = 24.dp

@Composable
fun DictionariesScreen(state: DictionariesState, onIntent: (DictionariesIntent) -> Unit) {
  Scaffold(title = stringResource(Res.string.dictionaries_title)) { _ ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(
            start = MonTheme.space.screenMargin,
            end = MonTheme.space.screenMargin,
            top = MonTheme.space.spacing5,
            bottom = MonTheme.space.spacing6,
          )
    ) {
      Column(
        modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing4),
      ) {
        val error = state.refreshError
        when {
          state.packs.isEmpty() && !state.isRefreshing && error != null ->
            CheckFailedNothingToShow(error, onIntent)
          state.packs.isEmpty() && !state.isRefreshing -> EmptyCatalog(onIntent)
          else -> PackList(state, onIntent)
        }
      }
    }
  }
}

@Composable
private fun ColumnScope.PackList(state: DictionariesState, onIntent: (DictionariesIntent) -> Unit) {
  if (state.isRefreshing) Checking()
  val error = state.refreshError
  if (!state.isRefreshing && error != null) {
    MonNotice(
      title = error.message(),
      body =
        if (state.installedIds.isNotEmpty())
          stringResource(Res.string.dictionaries_check_failed_kept)
        else null,
      tone = NoticeTone.Info,
      modifier = Modifier.widthIn(max = MAX_TEXT_WIDTH),
    )
  }
  Intro(state, onIntent)

  if (state.packs.isEmpty()) {
    // The first visit: nothing is known yet while the catalog loads.
    EmptySlot()
  } else if (state.installed.isNotEmpty() && state.available.isNotEmpty()) {
    SectionHeader(stringResource(Res.string.dictionaries_section_installed))
    PackGrid(state.installed, onIntent)
    SectionHeader(stringResource(Res.string.dictionaries_section_available))
    PackGrid(state.available, onIntent)
  } else {
    PackGrid(state.packs, onIntent)
  }

  DownloadFailures(state.packs)

  if (!state.isRefreshing) {
    YomuPillButton(
      label = stringResource(Res.string.dictionaries_check_again),
      variant = PillVariant.Outline,
      size = PillSize.Large,
      leadingIcon = Icon.Core.Refresh,
      onClick = { onIntent(DictionariesIntent.Refresh) },
    )
  }
}

@Composable
private fun Checking() {
  Column(
    modifier =
      Modifier.widthIn(max = MAX_TEXT_WIDTH).semantics { liveRegion = LiveRegionMode.Polite },
    verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing2),
  ) {
    Text(
      text = stringResource(Res.string.dictionaries_checking),
      style = MonTheme.typography.bodySmall,
      color = MonTheme.colors.textPrimary,
    )
    LinearProgressIndicator(
      modifier = Modifier.fillMaxWidth().height(2.dp),
      color = MonTheme.colors.secondary,
      trackColor = MonTheme.colors.listItem.background,
      gapSize = 0.dp,
    )
  }
}

/** One line above the packs, saying what matters most right now. */
@Composable
private fun Intro(state: DictionariesState, onIntent: (DictionariesIntent) -> Unit) {
  val ready = state.readyPackName
  if (ready != null) {
    MonNotice(
      title = stringResource(Res.string.dictionaries_ready_title, ready),
      body = stringResource(Res.string.dictionaries_ready_body),
      linkLabel = stringResource(Res.string.dictionaries_ready_link),
      onLinkClick = { onIntent(DictionariesIntent.OpenLibrary) },
      tone = NoticeTone.Success,
      modifier = Modifier.widthIn(max = MAX_TEXT_WIDTH),
    )
    return
  }
  val updates = state.packs.filter { it.state is PackState.UpdateAvailable }
  val text =
    when {
      state.packs.any { it.state is PackState.Downloading } ->
        stringResource(Res.string.dictionaries_intro_downloading)
      state.installedIds.isEmpty() -> stringResource(Res.string.dictionaries_intro_first_run)
      updates.size == 1 ->
        stringResource(Res.string.dictionaries_intro_update, updates[0].info.name)
      updates.size > 1 -> stringResource(Res.string.dictionaries_intro_updates)
      else -> return
    }
  Text(
    text = text,
    style = MonTheme.typography.bodySmall,
    color = MonTheme.colors.textPrimary,
    modifier = Modifier.widthIn(max = MAX_TEXT_WIDTH),
  )
}

@Composable
private fun SectionHeader(text: String) {
  Text(
    text = text.uppercase(),
    style = MonTheme.typography.labelMedium,
    color = MonTheme.colors.textPrimary,
    modifier = Modifier.padding(top = MonTheme.space.spacing1).semantics { heading() },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PackGrid(packs: List<Pack>, onIntent: (DictionariesIntent) -> Unit) {
  // Fixed-width tiles: two columns on a phone, more on a wide window up to MAX_CONTENT_WIDTH.
  // Tiles in a row share the height of the tallest one.
  FlowRow(
    modifier = Modifier.padding(top = ROOM_ABOVE_TILE - MonTheme.space.spacing4),
    horizontalArrangement = Arrangement.spacedBy(MonTheme.space.spacing2),
    verticalArrangement = Arrangement.spacedBy(ROOM_ABOVE_TILE),
  ) {
    packs.forEach { pack ->
      val id = pack.info.id
      PackTile(
        pack = pack,
        onOpen = { onIntent(DictionariesIntent.OpenPack(id)) },
        onDownload = { onIntent(DictionariesIntent.Download(id)) },
        modifier = Modifier.fillMaxRowHeight(),
      )
    }
  }
}

/**
 * The full message of each failed download, under the grid. Retrying an unsupported one cannot
 * help.
 */
@Composable
private fun DownloadFailures(packs: List<Pack>) {
  packs.forEach { pack ->
    val failed = pack.state as? PackState.Failed ?: return@forEach
    if (failed.error == PackError.UNSUPPORTED) return@forEach
    val message = failureMessage(pack, failed.error)
    MonNotice(
      title =
        if (packs.size > 1)
          stringResource(Res.string.dictionaries_error_for_pack, pack.info.name, message)
        else message,
      body = oldVersionWorks(failed),
      tone = NoticeTone.Error,
      modifier = Modifier.widthIn(max = MAX_TEXT_WIDTH),
    )
  }
}

@Composable
private fun EmptySlot() {
  Box(
    modifier =
      Modifier.padding(top = ROOM_ABOVE_TILE - MonTheme.space.spacing4)
        .size(TILE_WIDTH, TILE_MIN_HEIGHT)
        .dashedBorder(
          // paper60 is fine here: the slot is decoration and carries no text.
          color = MonTheme.colors.listItem.subtitle,
          strokeWidth = 1.dp,
          dashedLength = 6.dp,
          gapLength = 4.dp,
          cornerRadius = 12.dp,
        )
  )
}

/** The catalog was read, and no pack fits this version of the app. */
@Composable
private fun EmptyCatalog(onIntent: (DictionariesIntent) -> Unit) {
  EmptySlot()
  Column(
    modifier = Modifier.widthIn(max = MAX_TEXT_WIDTH),
    verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing1),
  ) {
    Text(
      text = stringResource(Res.string.dictionaries_empty),
      style = MonTheme.typography.titleSmall,
      color = MonTheme.colors.textPrimary,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text = stringResource(Res.string.dictionaries_empty_body),
      style = MonTheme.typography.bodySmall,
      color = MonTheme.colors.textPrimary,
    )
  }
  YomuPillButton(
    label = stringResource(Res.string.dictionaries_check_again),
    variant = PillVariant.Outline,
    size = PillSize.Large,
    leadingIcon = Icon.Core.Refresh,
    onClick = { onIntent(DictionariesIntent.Refresh) },
  )
}

/**
 * The check failed and nothing is installed: the one case that blocks the reader, so it gets a full
 * empty state with a prominent retry.
 */
@Composable
private fun CheckFailedNothingToShow(error: CatalogError, onIntent: (DictionariesIntent) -> Unit) {
  Column(
    modifier =
      Modifier.padding(top = 100.dp).widthIn(max = MAX_TEXT_WIDTH).semantics {
        liveRegion = LiveRegionMode.Polite
      },
    verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing3),
  ) {
    MonIcon(id = Icon.Core.ErrorCircle, primaryColor = MonTheme.colors.error, requiredSize = 32.dp)
    Text(
      text = stringResource(Res.string.dictionaries_check_failed_empty_title),
      style = MonTheme.typography.titleMedium,
      color = MonTheme.colors.textPrimary,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text =
        when (error) {
          CatalogError.NETWORK -> stringResource(Res.string.dictionaries_check_failed_empty_network)
          CatalogError.INVALID -> stringResource(Res.string.dictionaries_check_failed_empty_invalid)
        },
      style = MonTheme.typography.bodyMedium,
      color = MonTheme.colors.textPrimary,
    )
    YomuPillButton(
      label = stringResource(Res.string.dictionaries_check_again),
      variant = PillVariant.Filled,
      size = PillSize.Large,
      leadingIcon = Icon.Core.Refresh,
      onClick = { onIntent(DictionariesIntent.Refresh) },
      modifier = Modifier.padding(top = MonTheme.space.spacing3),
    )
  }
}
