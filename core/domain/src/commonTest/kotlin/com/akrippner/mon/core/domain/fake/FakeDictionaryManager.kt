package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.dictionary.PackReader
import com.akrippner.mon.core.dictionary.pack.CatalogError
import com.akrippner.mon.core.dictionary.pack.DictionaryManager
import com.akrippner.mon.core.dictionary.pack.Pack
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Only [readers] matters to the use case; the pack operations do nothing. */
class FakeDictionaryManager(private val dictionaries: List<PackReader> = emptyList()) :
  DictionaryManager {
  override val packs: StateFlow<List<Pack>> = MutableStateFlow(emptyList())

  /** If set, returned from the second [readers] call on, as after an update replaced a reader. */
  var afterFirstCall: List<PackReader>? = null
  var readersCalls = 0

  override suspend fun refreshCatalog(): CatalogError? = null

  override suspend fun install(packId: String) {}

  override suspend fun remove(packId: String) {}

  override suspend fun readers(): List<PackReader> =
    if (readersCalls++ == 0) dictionaries else afterFirstCall ?: dictionaries
}

class FakeDictionary(
  id: String = "fake",
  override val sourceLanguage: Language = Language.JAPANESE,
  override val glossLanguage: Language = Language.ENGLISH,
  private val entries: List<DictionaryEntry> = emptyList(),
  private val failure: Throwable? = null,
) : MonDictionary {
  override val source = DictionarySource(id, id, "attribution", "1")
  val queries = mutableListOf<LookupQuery>()

  override suspend fun lookup(query: LookupQuery): List<DictionaryEntry> {
    queries += query
    failure?.let { throw it }
    return entries
  }
}
