package com.akrippner.mon.feature.dictionaries

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.akrippner.mon.core.packs.CatalogError
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackError
import com.akrippner.mon.core.packs.PackState
import com.akrippner.mon.feature.dictionaries.generated.resources.Res
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_catalog_invalid
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_catalog_network
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_checksum
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_free_space
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_network
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_not_enough_space
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_old_version_works
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_remove
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_storage
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_error_unsupported
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_language_pair
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_not_compatible
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_reason_checksum
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_reason_network
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_reason_not_enough_space
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_reason_remove
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_reason_storage
import com.akrippner.mon.feature.dictionaries.generated.resources.dictionaries_size_mb
import com.akrippner.mon.feature.dictionaries.generated.resources.language_de
import com.akrippner.mon.feature.dictionaries.generated.resources.language_en
import com.akrippner.mon.feature.dictionaries.generated.resources.language_es
import com.akrippner.mon.feature.dictionaries.generated.resources.language_fr
import com.akrippner.mon.feature.dictionaries.generated.resources.language_hu
import com.akrippner.mon.feature.dictionaries.generated.resources.language_ja
import com.akrippner.mon.feature.dictionaries.generated.resources.language_nl
import com.akrippner.mon.feature.dictionaries.generated.resources.language_ru
import com.akrippner.mon.feature.dictionaries.generated.resources.language_sv
import org.jetbrains.compose.resources.stringResource

// Text shared by the tile, the list and the detail screen.

private const val BYTES_PER_MB = 1024L * 1024L

/** A size in whole megabytes, at least 1 MB: "38 MB". */
@Composable
internal fun megabytes(bytes: Long): String =
  stringResource(Res.string.dictionaries_size_mb, (bytes / BYTES_PER_MB).coerceAtLeast(1L))

/** "Japanese → English". A language Mon has no name for shows as its tag. */
@Composable
internal fun languagePair(info: CatalogPack): String =
  stringResource(
    Res.string.dictionaries_language_pair,
    languageName(info.sourceLanguage),
    languageName(info.glossLanguage),
  )

@Composable
private fun languageName(tag: String): String =
  when (tag.lowercase().substringBefore('-')) {
    "ja" -> stringResource(Res.string.language_ja)
    "en" -> stringResource(Res.string.language_en)
    "de" -> stringResource(Res.string.language_de)
    "fr" -> stringResource(Res.string.language_fr)
    "nl" -> stringResource(Res.string.language_nl)
    "es" -> stringResource(Res.string.language_es)
    "ru" -> stringResource(Res.string.language_ru)
    "sv" -> stringResource(Res.string.language_sv)
    "hu" -> stringResource(Res.string.language_hu)
    else -> tag
  }

@Composable
internal fun CatalogError.message(): String =
  when (this) {
    CatalogError.NETWORK -> stringResource(Res.string.dictionaries_error_catalog_network)
    CatalogError.INVALID -> stringResource(Res.string.dictionaries_error_catalog_invalid)
  }

/** The full sentence for a failed download. */
@Composable
internal fun PackError.message(): String =
  when (this) {
    PackError.NETWORK -> stringResource(Res.string.dictionaries_error_network)
    PackError.CHECKSUM -> stringResource(Res.string.dictionaries_error_checksum)
    PackError.STORAGE -> stringResource(Res.string.dictionaries_error_storage)
    PackError.NOT_ENOUGH_SPACE -> stringResource(Res.string.dictionaries_error_not_enough_space)
    PackError.UNSUPPORTED -> stringResource(Res.string.dictionaries_error_unsupported)
    PackError.REMOVE -> stringResource(Res.string.dictionaries_error_remove)
  }

/** Two or three words for a tile's status line. */
@Composable
internal fun PackError.shortReason(): String =
  when (this) {
    PackError.NETWORK -> stringResource(Res.string.dictionaries_reason_network)
    PackError.CHECKSUM -> stringResource(Res.string.dictionaries_reason_checksum)
    PackError.STORAGE -> stringResource(Res.string.dictionaries_reason_storage)
    PackError.NOT_ENOUGH_SPACE -> stringResource(Res.string.dictionaries_reason_not_enough_space)
    PackError.UNSUPPORTED -> stringResource(Res.string.dictionaries_not_compatible)
    PackError.REMOVE -> stringResource(Res.string.dictionaries_reason_remove)
  }

/**
 * Why [pack]'s download failed and what to do: the error, plus how much space to free when there
 * was too little. The installer needs room for the download and the unpacked file side by side.
 */
@Composable
internal fun failureMessage(pack: Pack, error: PackError): String {
  val message = error.message()
  if (error != PackError.NOT_ENOUGH_SPACE) return message
  val needed = megabytes(pack.info.sizeBytes + pack.info.unpackedSizeBytes)
  return message + " " + stringResource(Res.string.dictionaries_error_free_space, needed)
}

/** For a failed update: the old version is still installed and works. */
@Composable
internal fun oldVersionWorks(state: PackState.Failed): String? =
  state.installed
    ?.takeIf { state.error != PackError.REMOVE }
    ?.let { stringResource(Res.string.dictionaries_error_old_version_works, it.version) }

private val urlPattern = Regex("""https?://[^\s()]+""")

/** [text] with every URL in it made a tappable link, underlined and medium weight. */
internal fun linkified(text: String): AnnotatedString = buildAnnotatedString {
  val linkStyle =
    TextLinkStyles(
      SpanStyle(fontWeight = FontWeight.Medium, textDecoration = TextDecoration.Underline)
    )
  var start = 0
  for (match in urlPattern.findAll(text)) {
    // A sentence may end right after a URL; the full stop is not part of it.
    val url = match.value.trimEnd('.', ',', ';', ':')
    append(text.substring(start, match.range.first))
    withLink(LinkAnnotation.Url(url, linkStyle)) { append(url) }
    start = match.range.first + url.length
  }
  append(text.substring(start))
}
