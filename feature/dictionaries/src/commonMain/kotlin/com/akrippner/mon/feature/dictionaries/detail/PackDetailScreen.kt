package com.akrippner.mon.feature.dictionaries.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.NoticeTone
import com.akrippner.mon.core.designsystem.PillSize
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.component.MonConfirmDialog
import com.akrippner.mon.core.designsystem.component.MonNotice
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackError
import com.akrippner.mon.core.packs.PackState
import com.akrippner.mon.feature.dictionaries.PackStatusRow
import com.akrippner.mon.feature.dictionaries.TileKind
import com.akrippner.mon.feature.dictionaries.failureMessage
import com.akrippner.mon.feature.dictionaries.generated.resources.Res
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_download_size
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_frees
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_installed_version
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_languages
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_licence
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_on_device
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_title
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_detail_version
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_keep
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_remove
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_remove_body
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_remove_title
import com.akrippner.mon.feature.dictionaries.languagePair
import com.akrippner.mon.feature.dictionaries.linkified
import com.akrippner.mon.feature.dictionaries.megabytes
import com.akrippner.mon.feature.dictionaries.oldVersionWorks
import com.akrippner.mon.feature.dictionaries.onDisk
import com.akrippner.mon.feature.dictionaries.primaryActionLabel
import com.akrippner.mon.feature.dictionaries.tileKind
import org.jetbrains.compose.resources.stringResource

private val MAX_TEXT_WIDTH = 427.dp

/** One pack: its facts, the licence attribution its licence requires us to show, and Remove. */
@Composable
fun PackDetailScreen(state: PackDetailState, onIntent: (PackDetailIntent) -> Unit) {
  Scaffold(
    title = stringResource(Res.string.dictionaries_detail_title),
    navigationIcon = Icon.Core.ChevronLeft,
    onNavigationClick = { onIntent(PackDetailIntent.Back) },
  ) { _ ->
    val pack = state.pack ?: return@Scaffold
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
      PackDetail(pack, onIntent, Modifier.widthIn(max = MAX_TEXT_WIDTH))
    }
    if (state.confirmingRemove) RemoveDialog(pack, onIntent)
  }
}

@Composable
private fun PackDetail(pack: Pack, onIntent: (PackDetailIntent) -> Unit, modifier: Modifier) {
  val kind = pack.tileKind
  val info = pack.info
  Column(modifier = modifier) {
    Text(
      text = info.name,
      style = MonTheme.typography.titleMedium,
      color = MonTheme.colors.textPrimary,
      modifier = Modifier.semantics { heading() },
    )
    PackStatusRow(
      pack = pack,
      kind = kind,
      iconSize = 16.dp,
      style = MonTheme.typography.labelMedium,
      modifier = Modifier.padding(top = MonTheme.space.spacing2),
    )

    Column(
      modifier = Modifier.padding(top = MonTheme.space.spacing4),
      verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing2),
    ) {
      Fact(stringResource(Res.string.dictionaries_detail_languages), languagePair(info))
      Fact(stringResource(Res.string.dictionaries_detail_version), info.version)
      installedVersion(pack)?.let {
        Fact(stringResource(Res.string.dictionaries_detail_installed_version), it)
      }
      Fact(stringResource(Res.string.dictionaries_detail_download_size), megabytes(info.sizeBytes))
      Fact(
        stringResource(Res.string.dictionaries_detail_on_device),
        megabytes(info.unpackedSizeBytes),
      )
    }

    val failed = pack.state as? PackState.Failed
    if (failed != null && failed.error != PackError.UNSUPPORTED) {
      MonNotice(
        title = failureMessage(pack, failed.error),
        body = oldVersionWorks(failed),
        tone = NoticeTone.Error,
        modifier = Modifier.padding(top = MonTheme.space.spacing6),
      )
    }

    Text(
      text = stringResource(Res.string.dictionaries_detail_licence).uppercase(),
      style = MonTheme.typography.titleMedium,
      color = MonTheme.colors.textPrimary,
      modifier = Modifier.padding(top = MonTheme.space.spacing6).semantics { heading() },
    )
    // Required by the dictionary's licence: shown in full, as the catalog gives it.
    val attribution = remember(info.attribution) { linkified(info.attribution) }
    Text(
      text = attribution,
      style = MonTheme.typography.bodySmall,
      color = MonTheme.colors.textPrimary,
      modifier = Modifier.padding(top = MonTheme.space.spacing2),
    )

    Actions(pack, kind, onIntent)
  }
}

/** The version on disk, when it differs from the newest one shown as "Version". */
private fun installedVersion(pack: Pack): String? =
  pack.onDisk?.version?.takeIf { it != pack.info.version }

@Composable
private fun Fact(label: String, value: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(MonTheme.space.spacing4)) {
    Text(text = label, style = MonTheme.typography.bodySmall, color = MonTheme.colors.textPrimary)
    Text(
      text = value,
      style = MonTheme.typography.labelMedium,
      color = MonTheme.colors.textPrimary,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun Actions(pack: Pack, kind: TileKind, onIntent: (PackDetailIntent) -> Unit) {
  // Something is on disk and no download is running: the manager refuses to remove mid-download.
  val removable = pack.onDisk != null && pack.state !is PackState.Downloading
  val primary = primaryActionLabel(kind)
  if (primary == null && !removable) return
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = MonTheme.space.spacing6),
    verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing2),
  ) {
    if (primary != null) {
      YomuPillButton(
        label = primary,
        variant = PillVariant.Filled,
        size = PillSize.Large,
        onClick = { onIntent(PackDetailIntent.Download) },
        modifier = Modifier.fillMaxWidth(),
      )
    }
    if (removable) {
      YomuPillButton(
        label = stringResource(Res.string.dictionaries_remove),
        sublabel =
          stringResource(Res.string.dictionaries_detail_frees, megabytes(onDiskSize(pack))),
        variant = PillVariant.Outline,
        size = PillSize.Large,
        onClick = { onIntent(PackDetailIntent.AskRemove) },
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

/** The space the installed version takes, which is what Remove frees. */
private fun onDiskSize(pack: Pack): Long = (pack.onDisk ?: pack.info).unpackedSizeBytes

@Composable
private fun RemoveDialog(pack: Pack, onIntent: (PackDetailIntent) -> Unit) {
  MonConfirmDialog(
    title = stringResource(Res.string.dictionaries_remove_title, pack.info.name),
    text =
      stringResource(
        Res.string.dictionaries_remove_body,
        languagePair(pack.info),
        megabytes(pack.info.sizeBytes),
      ),
    confirmLabel = stringResource(Res.string.dictionaries_remove),
    dismissLabel = stringResource(Res.string.dictionaries_keep),
    onConfirm = { onIntent(PackDetailIntent.ConfirmRemove) },
    onDismiss = { onIntent(PackDetailIntent.CancelRemove) },
  )
}
