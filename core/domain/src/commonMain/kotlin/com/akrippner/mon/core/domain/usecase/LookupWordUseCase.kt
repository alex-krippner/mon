package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.dictionary.DictionaryClosedException
import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.dictionary.pack.DictionaryManager
import com.akrippner.mon.core.domain.model.LookupResult
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.MonToken
import kotlinx.coroutines.CancellationException

private const val MAX_ATTEMPTS = 3

/**
 * Looks a token up in every installed dictionary that translates [source] into [gloss]. The result
 * says whether there was no suitable dictionary at all, so the UI can offer to download one.
 */
class LookupWordUseCase(private val manager: DictionaryManager) {
  suspend fun execute(token: MonToken, source: Language, gloss: Language): LookupResult {
    val query =
      LookupQuery(
        lemma = token.dictionaryForm,
        surface = token.surface,
        // The pronunciation belongs to the surface form. It only helps pick between entries with
        // the same spelling when the surface is the dictionary form itself.
        reading = token.pronunciation.takeIf { token.surface == token.dictionaryForm },
      )

    repeat(MAX_ATTEMPTS) {
      val dictionaries =
        manager.installed().filter { it.sourceLanguage == source && it.glossLanguage == gloss }
      if (dictionaries.isEmpty()) return LookupResult.NoDictionaryInstalled
      try {
        val entries = dictionaries.flatMap { it.entriesFor(query) }
        return if (entries.isEmpty()) LookupResult.NotFound else LookupResult.Found(entries)
      } catch (e: DictionaryClosedException) {
        // An update or removal closed a reader after installed() handed it out. That says nothing
        // about the word, so ask the manager for the current readers and look again.
      }
    }
    return LookupResult.NotFound // packs were replaced on every attempt; practically unreachable
  }

  private suspend fun MonDictionary.entriesFor(query: LookupQuery): List<DictionaryEntry> =
    try {
      lookup(query)
    } catch (e: CancellationException) {
      throw e
    } catch (e: DictionaryClosedException) {
      throw e
    } catch (e: Exception) {
      // One damaged dictionary file must not break the word sheet or hide the other
      // dictionaries' results.
      emptyList()
    }
}
