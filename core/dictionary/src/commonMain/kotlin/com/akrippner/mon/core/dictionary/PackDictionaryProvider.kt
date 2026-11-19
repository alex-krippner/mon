package com.akrippner.mon.core.dictionary

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.packs.PackClosedException
import com.akrippner.mon.core.packs.PackManager

private const val MAX_ATTEMPTS = 3

private val log = Logger.withTag("PackDictionaryProvider")

/** The dictionaries of the installed packs. */
internal class PackDictionaryProvider(private val packs: PackManager) : DictionaryProvider {
  override suspend fun dictionariesFor(source: Language, gloss: Language): List<MonDictionary> =
    packs
      .installed()
      .filter { it.sourceLanguage == source && it.glossLanguage == gloss }
      .map { ReplacingDictionary(it, packs) }
}

/**
 * A pack reader is closed when its pack is updated or removed, after which its calls throw
 * [PackClosedException]. That says nothing about the word, so this asks the manager for the pack's
 * current reader and repeats the lookup. If the pack is gone, or keeps being replaced, the lookup
 * fails with [DictionaryUnavailableException].
 */
private class ReplacingDictionary(
  private val first: MonDictionary,
  private val packs: PackManager,
) : MonDictionary {
  override val source: DictionarySource = first.source
  override val sourceLanguage: Language = first.sourceLanguage
  override val glossLanguage: Language = first.glossLanguage

  override suspend fun lookup(query: LookupQuery): List<DictionaryEntry> {
    var reader = first
    repeat(MAX_ATTEMPTS) { attempt ->
      try {
        return reader.lookup(query)
      } catch (e: PackClosedException) {
        log.d(e) { "Reader of '${source.id}' was closed (attempt ${attempt + 1}/$MAX_ATTEMPTS)" }
        if (attempt == MAX_ATTEMPTS - 1) {
          throw DictionaryUnavailableException(
            "Pack '${source.id}' was replaced $MAX_ATTEMPTS times",
            e,
          )
        }
        reader =
          packs.installed().firstOrNull { it.source.id == source.id }
            ?: throw DictionaryUnavailableException("Pack '${source.id}' is no longer installed", e)
      }
    }
    error("unreachable") // the last attempt either returns or throws
  }
}
