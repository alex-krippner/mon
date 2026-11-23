package com.akrippner.mon.core.domain.usecase

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.DictionaryUnavailableException
import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.domain.model.LookupResult
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.MonToken
import kotlinx.coroutines.CancellationException

private val log = Logger.withTag("LookupWordUseCase")

/**
 * Looks a token up in every dictionary that translates [source] into [gloss]. The result says
 * whether there was no suitable dictionary at all, so the UI can offer to download one.
 */
class LookupWordUseCase(private val dictionaries: DictionaryProvider) {
  suspend fun execute(token: MonToken, source: Language, gloss: Language): LookupResult {
    val query =
      LookupQuery(
        lemma = token.dictionaryForm,
        surface = token.surface,
        // The pronunciation belongs to the surface form. It only helps pick between entries with
        // the same spelling when the surface is the dictionary form itself.
        reading = token.pronunciation.takeIf { token.surface == token.dictionaryForm },
      )

    val available = dictionaries.dictionariesFor(source, gloss)
    if (available.isEmpty()) return LookupResult.NoDictionaryInstalled

    var unavailable = 0
    val entries = available.flatMap { it.entriesFor(query) { unavailable++ } }
    if (entries.isNotEmpty()) return LookupResult.Found(entries)

    // If every dictionary vanished during the lookup (removed or replaced), there may be none left.
    val allUnavailable = unavailable == available.size
    if (allUnavailable && dictionaries.dictionariesFor(source, gloss).isEmpty()) {
      return LookupResult.NoDictionaryInstalled
    }
    // Some dictionary could not answer, so "not found" would be a claim we can't make.
    return if (unavailable > 0) LookupResult.Unavailable else LookupResult.NotFound
  }

  private suspend fun MonDictionary.entriesFor(
    query: LookupQuery,
    onUnavailable: () -> Unit,
  ): List<DictionaryEntry> =
    try {
      lookup(query)
    } catch (e: CancellationException) {
      throw e
    } catch (e: DictionaryUnavailableException) {
      // The dictionary can't answer right now. It says nothing about the word, so skip it.
      log.w(e) { "Dictionary '${source.id}' is unavailable ($sourceLanguage -> $glossLanguage)" }
      onUnavailable()
      emptyList()
    } catch (e: Exception) {
      // One damaged dictionary file must not break the word sheet or hide the other
      // dictionaries' results.
      log.w(e) { "Lookup failed in dictionary '${source.id}' ($sourceLanguage -> $glossLanguage)" }
      emptyList()
    }
}
