package com.akrippner.mon.tools.jmdict

import kotlinx.serialization.Serializable

// The parts of the jmdict-simplified format (https://github.com/scriptin/jmdict-simplified) that
// the
// converter uses. Everything else in the file is ignored. Defaults keep parsing lenient.

@Serializable
internal class SimplifiedDictionary(
  val version: String,
  val languages: List<String>,
  val commonOnly: Boolean,
  val dictDate: String,
  /** Tag code -> English description, e.g. "v1" -> "Ichidan verb". */
  val tags: Map<String, String>,
  val words: List<SimplifiedWord>,
)

@Serializable
internal class SimplifiedWord(
  val id: String,
  val kanji: List<SimplifiedKanji> = emptyList(),
  val kana: List<SimplifiedKana> = emptyList(),
  val sense: List<SimplifiedSense> = emptyList(),
)

@Serializable internal class SimplifiedKanji(val text: String, val common: Boolean = false)

@Serializable
internal class SimplifiedKana(
  val text: String,
  val common: Boolean = false,
  /** "*" means every kanji spelling; empty means none. */
  val appliesToKanji: List<String> = emptyList(),
)

@Serializable
internal class SimplifiedSense(
  val partOfSpeech: List<String> = emptyList(),
  val field: List<String> = emptyList(),
  val dialect: List<String> = emptyList(),
  val misc: List<String> = emptyList(),
  val info: List<String> = emptyList(),
  val gloss: List<SimplifiedGloss> = emptyList(),
)

@Serializable internal class SimplifiedGloss(val text: String)
