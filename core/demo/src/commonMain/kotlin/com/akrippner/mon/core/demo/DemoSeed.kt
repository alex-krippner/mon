package com.akrippner.mon.core.demo

import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.GrammaticalGender
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.Sense
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** How the seed file is written and read. */
val DemoSeedJson = Json {
  prettyPrint = true
  // A seed written by a newer tool still loads.
  ignoreUnknownKeys = true
}

/**
 * Everything the web demo starts with: readings that are already tokenized and the dictionary
 * entries for their words, so no tokenizer or dictionary pack is needed in the browser.
 */
@Serializable
data class DemoSeed(
  val dictionary: DemoDictionaryInfo,
  val readings: List<DemoReading>,
  val lookups: List<DemoLookup>,
) {
  fun toReadings(): List<Reading> = readings.map { it.toReading() }
}

@Serializable
data class DemoDictionaryInfo(
  val id: String,
  val name: String,
  val attribution: String,
  val version: String,
  val sourceLanguage: String,
  val glossLanguage: String,
) {
  internal fun toSource() = DictionarySource(id, name, attribution, version)
}

@Serializable
data class DemoReading(
  val id: String,
  val title: String,
  val text: String,
  val translation: String,
  val sourceLanguage: String,
  val translationLanguage: String,
  val tokens: List<DemoToken>,
  val createdAt: Long,
) {
  fun toReading() =
    Reading(
      id = id,
      title = title,
      text = text,
      translation = translation,
      sourceLanguage = Language.fromTag(sourceLanguage),
      translationLanguage = Language.fromTag(translationLanguage),
      tokens = tokens.map { it.toToken() },
      isTokenized = true,
      createdAt = createdAt,
    )
}

@Serializable
data class DemoToken(
  val surface: String,
  val dictionaryForm: String,
  val pronunciation: String,
  val partOfSpeech: List<String>,
) {
  fun toToken() = MonToken(surface, dictionaryForm, pronunciation, partOfSpeech)

  companion object {
    fun from(token: MonToken) =
      DemoToken(token.surface, token.dictionaryForm, token.pronunciation, token.partOfSpeech)
  }
}

/** The entries one look-up found. The query is stored as the app made it, so it matches again. */
@Serializable
data class DemoLookup(
  val lemma: String,
  val surface: String? = null,
  val reading: String? = null,
  val entries: List<DemoEntry>,
) {
  internal fun matches(query: LookupQuery) =
    lemma == query.lemma && surface == query.surface && reading == query.reading

  companion object {
    fun from(query: LookupQuery, entries: List<DictionaryEntry>) =
      DemoLookup(query.lemma, query.surface, query.reading, entries.map { DemoEntry.from(it) })
  }
}

@Serializable
data class DemoEntry(
  val id: String,
  val headwords: List<DemoHeadword>,
  val senses: List<DemoSense>,
  val labels: List<DemoLabel> = emptyList(),
) {
  internal fun toEntry(source: DictionarySource) =
    DictionaryEntry(
      id = id,
      source = source,
      headwords = headwords.map { Headword(it.text, it.reading) },
      senses = senses.map { Sense(it.glosses, it.labels.map { label -> label.toLabel() }) },
      labels = labels.map { it.toLabel() },
    )

  companion object {
    fun from(entry: DictionaryEntry) =
      DemoEntry(
        id = entry.id,
        headwords = entry.headwords.map { DemoHeadword(it.text, it.reading) },
        senses =
          entry.senses.map { sense ->
            DemoSense(sense.glosses, sense.labels.map { DemoLabel.from(it) })
          },
        labels = entry.labels.map { DemoLabel.from(it) },
      )
  }
}

@Serializable data class DemoHeadword(val text: String, val reading: String? = null)

@Serializable
data class DemoSense(val glosses: List<String>, val labels: List<DemoLabel> = emptyList())

/** [Label] is a sealed type, so it is stored as a kind and a text. */
@Serializable
data class DemoLabel(val kind: String, val text: String) {
  internal fun toLabel(): Label =
    when (kind) {
      PART_OF_SPEECH -> Label.PartOfSpeech(text)
      GENDER -> Label.Gender(GrammaticalGender.valueOf(text))
      REGISTER -> Label.Register(text)
      DOMAIN -> Label.Domain(text)
      FREQUENCY -> Label.Frequency(text)
      else -> Label.Other(text)
    }

  companion object {
    private const val PART_OF_SPEECH = "pos"
    private const val GENDER = "gender"
    private const val REGISTER = "register"
    private const val DOMAIN = "domain"
    private const val FREQUENCY = "frequency"
    private const val OTHER = "other"

    fun from(label: Label): DemoLabel =
      when (label) {
        is Label.PartOfSpeech -> DemoLabel(PART_OF_SPEECH, label.text)
        is Label.Gender -> DemoLabel(GENDER, label.gender.name)
        is Label.Register -> DemoLabel(REGISTER, label.text)
        is Label.Domain -> DemoLabel(DOMAIN, label.text)
        is Label.Frequency -> DemoLabel(FREQUENCY, label.text)
        is Label.Other -> DemoLabel(OTHER, label.text)
      }
  }
}
