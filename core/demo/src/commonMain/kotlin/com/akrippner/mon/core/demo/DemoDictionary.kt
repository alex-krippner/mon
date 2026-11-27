package com.akrippner.mon.core.demo

import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery

/**
 * Answers look-ups from the entries stored in the seed. A word that no demo reading contains is
 * "not found".
 */
class DemoDictionary(private val seed: DemoSeed) : MonDictionary {
  override val source: DictionarySource = seed.dictionary.toSource()
  override val sourceLanguage: Language = Language.fromTag(seed.dictionary.sourceLanguage)
  override val glossLanguage: Language = Language.fromTag(seed.dictionary.glossLanguage)

  override suspend fun lookup(query: LookupQuery): List<DictionaryEntry> {
    // The seed holds the query exactly as the app made it. The lemma alone is the fallback, so a
    // small change in how the app builds its query degrades to a less exact match, not to nothing.
    val lookup =
      seed.lookups.firstOrNull { it.matches(query) }
        ?: seed.lookups.firstOrNull { it.lemma == query.lemma }
    return lookup?.entries.orEmpty().map { it.toEntry(source) }
  }
}

/** Offers the [DemoDictionary] for its language pair. */
class DemoDictionaryProvider(seed: DemoSeed) : DictionaryProvider {
  private val dictionary = DemoDictionary(seed)

  override suspend fun dictionariesFor(source: Language, gloss: Language): List<MonDictionary> =
    if (source == dictionary.sourceLanguage && gloss == dictionary.glossLanguage) {
      listOf(dictionary)
    } else {
      emptyList()
    }
}
