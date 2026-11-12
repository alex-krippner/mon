package com.akrippner.mon.core.model.data

/** Where an entry comes from. [attribution] is the licence acknowledgement shown to the user. */
data class DictionarySource(
  val id: String,
  val name: String,
  val attribution: String,
  val version: String,
)

/**
 * One way a word is written. [reading] is null for languages or entries without a separate reading.
 */
data class Headword(val text: String, val reading: String? = null)

/** One meaning of a word. */
data class Sense(val glosses: List<String>, val labels: List<Label> = emptyList())

/**
 * One word as one dictionary describes it. [id] is prefixed with the source, e.g. "jmdict:1358280".
 */
data class DictionaryEntry(
  val id: String,
  val source: DictionarySource,
  val headwords: List<Headword>,
  val senses: List<Sense>,
  val labels: List<Label> = emptyList(),
)

/**
 * What the caller asks for. [lemma] is the main key, [surface] is a fallback, and [reading] is only
 * a ranking hint, because the tokenizer gives the reading of the inflected form.
 */
data class LookupQuery(val lemma: String, val surface: String? = null, val reading: String? = null)

sealed interface Label {
  data class PartOfSpeech(val text: String) : Label

  data class Gender(val gender: GrammaticalGender) : Label

  data class Register(val text: String) : Label

  data class Domain(val text: String) : Label

  data class Frequency(val text: String) : Label

  data class Other(val text: String) : Label
}

enum class GrammaticalGender {
  MASCULINE,
  FEMININE,
  NEUTRAL,
}
