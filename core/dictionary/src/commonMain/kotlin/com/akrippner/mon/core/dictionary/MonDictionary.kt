package com.akrippner.mon.core.dictionary

import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery

/**
 * A dictionary for one language pair. Implementations decide how they store and search their data.
 */
interface MonDictionary {
  val source: DictionarySource
  /** Language of the headwords. */
  val sourceLanguage: Language
  /** Language of the glosses. */
  val glossLanguage: Language

  /**
   * Entries for [query], best match first. Empty when the word isn't found.
   *
   * @throws DictionaryClosedException if the dictionary was closed in the meantime.
   */
  suspend fun lookup(query: LookupQuery): List<DictionaryEntry>
}

/**
 * The dictionary was closed because its pack is being updated or removed, so it can answer nothing.
 * This is not "word not found": fetch the dictionaries again and repeat the lookup.
 */
class DictionaryClosedException : IllegalStateException("The dictionary is closed")
