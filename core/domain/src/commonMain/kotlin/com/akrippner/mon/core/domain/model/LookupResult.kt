package com.akrippner.mon.core.domain.model

import com.akrippner.mon.core.model.data.DictionaryEntry

sealed interface LookupResult {
  data class Found(val entries: List<DictionaryEntry>) : LookupResult

  /** A dictionary for the language pair is installed, but it has nothing for this word. */
  data object NotFound : LookupResult

  /** No installed dictionary matches the language pair; the UI offers to download one. */
  data object NoDictionaryInstalled : LookupResult

  /**
   * Nothing was found, but at least one dictionary could not answer right now (for example, it was
   * being replaced). This is not [NotFound]: asking again later may find the word.
   */
  data object Unavailable : LookupResult
}
