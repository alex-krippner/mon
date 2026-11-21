package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery

/** Offers the dictionaries that match the language pair, as the real provider does. */
class FakeDictionaryProvider(private val dictionaries: List<MonDictionary> = emptyList()) :
  DictionaryProvider {
  override suspend fun dictionariesFor(source: Language, gloss: Language): List<MonDictionary> =
    dictionaries.filter { it.sourceLanguage == source && it.glossLanguage == gloss }
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
