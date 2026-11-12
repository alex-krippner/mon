package com.akrippner.mon.tools.jmdict

import com.akrippner.mon.core.dictionary.jmdict.JmdictRecord
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.Sense

/** `misc` tags that say how a word is used (register); every other `misc` tag becomes `Other`. */
private val REGISTER_TAGS =
  setOf(
    "col",
    "fam",
    "hon",
    "hum",
    "pol",
    "sl",
    "vulg",
    "derog",
    "joc",
    "male",
    "fem",
    "poet",
    "arch",
    "obs",
    "obsc",
    "rare",
  )

/**
 * Maps one jmdict-simplified word to a [JmdictRecord], or null if it has nothing to show (no
 * spelling or no gloss). The `common` frequency label is not added here: the reader derives it from
 * the [JmdictRecord.common] column.
 */
internal fun SimplifiedWord.toRecord(tags: Map<String, String>): JmdictRecord? {
  val headwords = headwords()
  val senses = sense.mapNotNull { it.toSense(tags) }
  if (headwords.isEmpty() || senses.isEmpty()) return null
  return JmdictRecord(
    id = id.toLong(),
    common = kanji.any { it.common } || kana.any { it.common },
    senses = senses,
    headwords = headwords,
  )
}

private fun SimplifiedKana.appliesTo(kanjiText: String) =
  "*" in appliesToKanji || kanjiText in appliesToKanji

/**
 * One headword per kanji spelling and applicable reading, so every reading stays searchable. A
 * reading that belongs to no kanji (kana-only words, "usually kana" forms) is a headword itself.
 */
private fun SimplifiedWord.headwords(): List<Headword> {
  val withKanji =
    kanji.flatMap { spelling ->
      val readings = kana.filter { it.appliesTo(spelling.text) }
      if (readings.isEmpty()) listOf(Headword(spelling.text))
      else readings.map { Headword(spelling.text, it.text) }
    }
  val kanaOnly = kana.filter { reading -> kanji.none { reading.appliesTo(it.text) } }
  return withKanji + kanaOnly.map { Headword(it.text) }
}

private fun SimplifiedSense.toSense(tags: Map<String, String>): Sense? {
  val glosses = gloss.map { it.text.trim() }.filter { it.isNotEmpty() }
  if (glosses.isEmpty()) return null
  fun describe(code: String) = tags[code] ?: code // an unknown code is still better than nothing
  val labels = buildList {
    partOfSpeech.forEach { add(Label.PartOfSpeech(describe(it))) }
    field.forEach { add(Label.Domain(describe(it))) }
    misc.forEach {
      add(if (it in REGISTER_TAGS) Label.Register(describe(it)) else Label.Other(describe(it)))
    }
    dialect.forEach { add(Label.Other(describe(it))) }
    info.forEach { add(Label.Other(it)) }
  }
  return Sense(glosses, labels)
}
