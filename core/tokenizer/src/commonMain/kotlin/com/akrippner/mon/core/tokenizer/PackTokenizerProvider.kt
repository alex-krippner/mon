package com.akrippner.mon.core.tokenizer

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackClosedException
import com.akrippner.mon.core.packs.PackManager
import com.akrippner.mon.core.packs.PackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val MAX_ATTEMPTS = 3

private val log = Logger.withTag("PackTokenizerProvider")

/** The tokenizers of the installed packs. */
internal class PackTokenizerProvider(private val packs: PackManager) : TokenizerProvider {
  override suspend fun tokenizerFor(language: Language): Tokenizer? =
    packs
      .tokenizers()
      .firstOrNull { it.language == language }
      ?.let { ReplacingTokenizer(it, packs) }

  /**
   * Emits once when collected, for the state at that moment, and again when a tokenizer pack was
   * installed, updated or removed; not for other kinds of packs. A download in progress changes
   * nothing: the old version stays usable until the new one replaces it.
   */
  override val changes: Flow<Unit> =
    packs.packs
      .map { list -> list.mapNotNull { it.installed() }.map { "${it.id}:${it.sha256}" }.sorted() }
      .distinctUntilChanged()
      // Only tokenizer packs matter. readers() is cheap, and runs only when the installed set
      // changed.
      .map { packs.tokenizers().map { it.id } }
      .distinctUntilChanged()
      .map {}
}

private suspend fun PackManager.tokenizers(): List<Tokenizer> =
  readers().filterIsInstance<Tokenizer>()

/** The version of the pack that is on disk, if any. */
private fun Pack.installed(): CatalogPack? =
  when (val state = state) {
    PackState.Installed -> info
    PackState.NotInstalled -> null
    is PackState.UpdateAvailable -> state.installed
    is PackState.Downloading -> state.installed
    is PackState.Failed -> state.installed
  }

/**
 * A pack reader is closed when its pack is updated or removed, after which its calls throw
 * [PackClosedException]. That says nothing about the text, so this asks the manager for the pack's
 * current reader of the same version (same [Tokenizer.id]) and repeats the call, so the id a
 * reading stores is the one of the reader that produced its tokens. If that version is gone
 * (removed or updated), or keeps being replaced, the call fails with
 * [TokenizerUnavailableException].
 */
private class ReplacingTokenizer(private val first: Tokenizer, private val packs: PackManager) :
  Tokenizer {
  override val language: Language = first.language
  override val id: String = first.id

  override suspend fun tokenize(text: String): List<MonToken> {
    var tokenizer = first
    repeat(MAX_ATTEMPTS) { attempt ->
      try {
        return tokenizer.tokenize(text)
      } catch (e: PackClosedException) {
        log.d(e) { "Tokenizer '$id' was closed (attempt ${attempt + 1}/$MAX_ATTEMPTS)" }
        if (attempt == MAX_ATTEMPTS - 1) {
          throw TokenizerUnavailableException("Tokenizer '$id' was replaced $MAX_ATTEMPTS times", e)
        }
        tokenizer =
          packs.tokenizers().firstOrNull { it.id == id }
            ?: throw TokenizerUnavailableException("Tokenizer '$id' is no longer installed", e)
      }
    }
    error("unreachable") // the last attempt either returns or throws
  }
}
