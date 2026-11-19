package com.akrippner.mon.core.dictionary

import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.packs.PackReader

/**
 * A reader that looks words up, for one language pair. Implementations decide how they store and
 * search their data.
 */
interface MonDictionary : PackReader {
  /**
   * Entries for [query], best match first. Empty when the word isn't found.
   *
   * @throws PackClosedException if the dictionary was closed in the meantime.
   */
  suspend fun lookup(query: LookupQuery): List<DictionaryEntry>
}
