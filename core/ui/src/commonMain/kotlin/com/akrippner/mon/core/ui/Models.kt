package com.akrippner.mon.core.ui

/**
 * A single tokenized word as produced by Sudachi + annotation layer.
 *
 * @param surface The surface form (text as written: 先生, 食べて…)
 * @param reading Kana reading for furigana overlay (せんせい)
 * @param dictionaryForm Form used to query JMdict (先生, 食べる…)
 * @param state Visual state driven by ReaderViewModel
 */
data class DisplayToken(
  val surface: String,
  val reading: String,
  val dictionaryForm: String,
  val state: TokenState = TokenState.Default,
)

/**
 * All visual states a reader token can occupy. Drive this from ReaderUiState.selectedToken and
 * annotation presence.
 */
sealed interface TokenState {
  /** Plain — no interaction, no annotation */
  data object Default : TokenState

  /** Currently tapped — red highlight + underline */
  data object Selected : TokenState

  /** Word saved to vocab (red dot + dashed underline) */
  data object Annotated : TokenState

  /** Word saved with a custom note (subtle bg + underline) */
  data object Noted : TokenState

  /** Punctuation — renders but is not tappable */
  data object Punctuation : TokenState
}

/** A JMdict sense entry for display in the lookup sheet */
data class Sense(
  val posLabel: String?, // 名詞, 動詞, etc.; null if the dictionary gives none
  val glosses: List<String>, // ["teacher", "master", "doctor"]
  val usageNote: String? = null,
)

/** A saved vocabulary word */
data class VocabEntry(
  val id: Long,
  val surface: String, // TODO: Rename to vocab?
  val reading: String,
  val definition: String,
  val jlptLevel: String?,
  val sourceTitle: String?,
  val userNote: String? = null,
)

data class JmdictEntry(
  val surface: String,
  val reading: String,
  val jlptLevel: String?, // "N5", "N4", … or null
  val senses: List<Sense>,
)
