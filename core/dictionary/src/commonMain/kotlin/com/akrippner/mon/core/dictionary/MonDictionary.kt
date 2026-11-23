package com.akrippner.mon.core.dictionary

import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery

/**
 * Looks words up for one language pair. Implementations decide how they store and search their
 * data: a pack reader reads a file, an online dictionary would ask a server.
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
   * @throws DictionaryUnavailableException if the dictionary can't answer right now (it was
   *   replaced, removed or can't be reached). This says nothing about the word.
   */
  suspend fun lookup(query: LookupQuery): List<DictionaryEntry>
}

/** Where to find the dictionaries for a language pair. The app doesn't care how they are stored. */
interface DictionaryProvider {
  /** The dictionaries that translate [source] into [gloss]. Empty if there is none. */
  suspend fun dictionariesFor(source: Language, gloss: Language): List<MonDictionary>
}

/** A dictionary can't answer right now. This is not "nothing found"; the word may well be there. */
class DictionaryUnavailableException(message: String, cause: Throwable? = null) :
  Exception(message, cause)
