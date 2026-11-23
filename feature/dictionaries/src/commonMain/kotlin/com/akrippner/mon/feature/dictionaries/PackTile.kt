package com.akrippner.mon.feature.dictionaries

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.PillSize
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.component.ReelPhase
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.component.reelProgress
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.stateLayer
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackError
import com.akrippner.mon.core.packs.PackState
import com.akrippner.mon.feature.dictionaries.generated.resources.Res
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_download
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_downloading
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_installed
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_installing
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_needs_newer_app
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_not_compatible
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_tile_download_size
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_tile_new_version
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_tile_of_size
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_tile_on_device
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_tile_using
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_tile_version
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_try_again
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_update
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_update_available
import org.jetbrains.compose.resources.stringResource

internal val TILE_WIDTH = 137.dp
internal val TILE_MIN_HEIGHT = 176.dp
private val TILE_SHAPE = RoundedCornerShape(12.dp)

/** What a tile shows. Downloading at 100 % is shown as installing: verifying and unpacking. */
internal enum class TileKind {
  NotInstalled,
  Downloading,
  Installing,
  Installed,
  Update,
  Failed,
  FailedKept,
  /** A removal failed: the pack is still installed. Removing again is done from the details. */
  RemoveFailed,
  Unsupported,
}

internal val Pack.tileKind: TileKind
  get() =
    when (val s = state) {
      PackState.NotInstalled -> TileKind.NotInstalled
      is PackState.Downloading ->
        if (s.progress >= 1f) TileKind.Installing else TileKind.Downloading
      PackState.Installed -> TileKind.Installed
      is PackState.UpdateAvailable -> TileKind.Update
      is PackState.Failed ->
        when {
          // Retrying cannot help: this app version cannot read the pack.
          s.error == PackError.UNSUPPORTED -> TileKind.Unsupported
          s.error == PackError.REMOVE -> TileKind.RemoveFailed
          s.installed != null -> TileKind.FailedKept
          else -> TileKind.Failed
        }
    }

/** The version on disk, or null if nothing is installed. During an update it is the old one. */
internal val Pack.onDisk: CatalogPack?
  get() =
    when (val s = state) {
      PackState.NotInstalled -> null
      PackState.Installed -> info
      is PackState.UpdateAvailable -> s.installed
      is PackState.Failed -> s.installed
      is PackState.Downloading -> s.installed
    }

/** The label of the tile's main button, or null if it has none. */
@Composable
internal fun primaryActionLabel(kind: TileKind): String? =
  when (kind) {
    TileKind.NotInstalled -> stringResource(Res.string.dictionaries_download)
    TileKind.Update -> stringResource(Res.string.dictionaries_update)
    TileKind.Failed,
    TileKind.FailedKept -> stringResource(Res.string.dictionaries_try_again)
    else -> null
  }

/**
 * One pack in the grid: 137 dp wide and at least 176 dp high, growing for a long name. A tap opens
 * the pack's details; the button downloads, updates or retries. While downloading, Mon reels the
 * pack in along the tile's outline, so leave 24 dp free above the tile.
 */
@Composable
internal fun PackTile(
  pack: Pack,
  onOpen: () -> Unit,
  onDownload: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val kind = pack.tileKind
  val progress = (pack.state as? PackState.Downloading)?.progress ?: 1f
  val phase =
    when (kind) {
      TileKind.Downloading -> ReelPhase.Reeling
      TileKind.Installing -> ReelPhase.Landed
      else -> ReelPhase.Idle
    }
  val interactionSource = remember { MutableInteractionSource() }

  Column(
    modifier =
      modifier
        .width(TILE_WIDTH)
        .heightIn(min = TILE_MIN_HEIGHT)
        .reelProgress(progress, phase)
        .stateLayer(interactionSource, TILE_SHAPE)
        .clip(TILE_SHAPE)
        .background(MonTheme.colors.listItem.background)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          role = Role.Button,
          onClick = onOpen,
        )
        .then(
          if (kind == TileKind.Downloading) {
            Modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }
          } else {
            Modifier
          }
        )
        .padding(12.dp),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Column {
      Text(
        text = pack.info.name,
        style = MonTheme.typography.titleSmall,
        color = MonTheme.colors.textPrimary,
        modifier = Modifier.heightIn(min = 40.dp),
      )
      Text(
        text = languagePair(pack.info),
        style = MonTheme.typography.labelSmall,
        color = MonTheme.colors.textPrimary,
        modifier = Modifier.padding(top = 4.dp),
      )
      Text(
        text = versionLine(pack, kind),
        style = MonTheme.typography.labelSmall,
        color = MonTheme.colors.textPrimary,
        modifier = Modifier.padding(top = 2.dp),
      )
      // The status and the bottom slot cross-fade when the kind changes, so a finished download
      // turns into "Installed" instead of jumping.
      Crossfade(targetState = kind, animationSpec = tween(450), label = "tile status") { k ->
        PackStatusRow(
          pack = pack,
          kind = k,
          iconSize = MonTheme.sizes.iconTiny,
          style = MonTheme.typography.labelSmall,
          modifier = Modifier.padding(top = 6.dp),
        )
      }
    }
    Crossfade(
      targetState = kind,
      animationSpec = tween(450),
      label = "tile action",
      modifier = Modifier.padding(top = 8.dp),
    ) { k ->
      TileBottom(pack, k, progress, onDownload)
    }
  }
}

@Composable
private fun versionLine(pack: Pack, kind: TileKind): String {
  val kept = if (kind == TileKind.FailedKept) pack.onDisk else null
  return when {
    kind == TileKind.Update ->
      stringResource(Res.string.dictionaries_tile_new_version, pack.info.version)
    kept != null -> stringResource(Res.string.dictionaries_tile_using, kept.version)
    else -> stringResource(Res.string.dictionaries_tile_version, pack.info.version)
  }
}

/**
 * The status line of a pack, with an icon where the state needs one. Shared by the tile and the
 * detail screen. All text is ink: the accent and the error red go on icons and short reasons.
 */
@Composable
internal fun PackStatusRow(
  pack: Pack,
  kind: TileKind,
  iconSize: Dp,
  style: TextStyle,
  modifier: Modifier = Modifier,
) {
  val colors = MonTheme.colors
  val failure = (pack.state as? PackState.Failed)?.error
  val (icon, iconColor) =
    when (kind) {
      TileKind.Installed -> Icon.Core.CheckCircle to colors.secondary
      TileKind.Failed,
      TileKind.FailedKept,
      TileKind.RemoveFailed -> Icon.Core.ErrorCircle to colors.error
      TileKind.Unsupported -> Icon.Core.Block to colors.textPrimary
      else -> null to Color.Unspecified
    }
  val text =
    when (kind) {
      TileKind.NotInstalled ->
        stringResource(Res.string.dictionaries_tile_download_size, megabytes(pack.info.sizeBytes))
      TileKind.Downloading -> stringResource(Res.string.dictionaries_downloading)
      TileKind.Installing -> stringResource(Res.string.dictionaries_installing)
      TileKind.Installed -> stringResource(Res.string.dictionaries_installed)
      TileKind.Update -> stringResource(Res.string.dictionaries_update_available)
      TileKind.Failed,
      TileKind.FailedKept,
      TileKind.RemoveFailed -> failure?.shortReason().orEmpty()
      TileKind.Unsupported -> stringResource(Res.string.dictionaries_not_compatible)
    }
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (icon != null) {
      MonIcon(id = icon, primaryColor = iconColor, requiredSize = iconSize)
    }
    Text(
      text = text,
      style = style,
      // red70 on paper70 meets 4.5:1; vermilion does not, so "Update available" stays ink.
      color =
        if (kind == TileKind.Failed || kind == TileKind.FailedKept || kind == TileKind.RemoveFailed)
          colors.error
        else colors.textPrimary,
    )
  }
}

@Composable
private fun TileBottom(pack: Pack, kind: TileKind, progress: Float, onDownload: () -> Unit) {
  Box(modifier = Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.BottomStart) {
    val action = primaryActionLabel(kind)
    when {
      action != null ->
        YomuPillButton(
          label = action,
          variant = PillVariant.Filled,
          size = PillSize.Large,
          onClick = onDownload,
          modifier = Modifier.fillMaxWidth(),
        )
      kind == TileKind.Downloading || kind == TileKind.Installing ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(
            text = "${(progress * 100).toInt()}%",
            style = MonTheme.typography.titleLarge,
            color = MonTheme.colors.textPrimary,
            modifier = Modifier.alignByBaseline(),
          )
          Text(
            text =
              stringResource(Res.string.dictionaries_tile_of_size, megabytes(pack.info.sizeBytes)),
            style = MonTheme.typography.labelSmall,
            color = MonTheme.colors.textPrimary,
            modifier = Modifier.alignByBaseline(),
          )
        }
      kind == TileKind.Installed ->
        FootLine(
          stringResource(
            Res.string.dictionaries_tile_on_device,
            megabytes(pack.info.unpackedSizeBytes),
          )
        )
      kind == TileKind.Unsupported ->
        FootLine(stringResource(Res.string.dictionaries_needs_newer_app))
    }
  }
}

@Composable
private fun FootLine(text: String) {
  Text(text = text, style = MonTheme.typography.labelSmall, color = MonTheme.colors.textPrimary)
}
