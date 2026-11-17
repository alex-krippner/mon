package com.akrippner.mon.core.dictionary.jmdict

import com.akrippner.mon.core.model.data.Language
import java.text.Normalizer
import java.util.Locale

/** The meta row that says how a file's lookup keys are normalized. Absent means not at all. */
internal const val KEY_NORMALIZATION = "keyNormalization"

/**
 * How the keys in `lookup_key` are normalized. The writer normalizes every key and records the rule
 * in the file; the reader applies the file's rule to each query. The reader never derives the rule
 * from the pack's language, so the two cannot disagree.
 *
 * Never change what an existing value does: files with that value are already published. A new rule
 * is a new value.
 *
 * @property id the value of the [KEY_NORMALIZATION] meta row; null means the file has no such row.
 */
internal enum class KeyNormalization(val id: String?) {
  /** Keys are stored as they are. Every Japanese pack, including all published before this. */
  NONE(null) {
    override fun apply(text: String) = text
  },

  /**
   * Unicode NFC, then lower case: "HAUS" and "Haus" find "haus", and a decomposed "ä" finds the
   * composed one. This is not case folding, so "STRASSE" does not find "Straße".
   */
  NFC_LOWER("nfc-lower") {
    override fun apply(text: String) =
      Normalizer.normalize(text, Normalizer.Form.NFC).lowercase(Locale.ROOT)
  };

  abstract fun apply(text: String): String

  companion object {
    /**
     * The rule the writer uses for a pack whose headwords are in [language]. Japanese has no case,
     * and its packs must stay byte-identical to the published ones.
     */
    fun forLanguage(language: Language): KeyNormalization =
      if (language == Language.JAPANESE) NONE else NFC_LOWER

    /** The rule for a meta [value] (null: no row), or null if this app does not know the rule. */
    fun fromMeta(value: String?): KeyNormalization? = entries.firstOrNull { it.id == value }
  }
}
