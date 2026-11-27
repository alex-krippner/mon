package com.akrippner.mon.feature.reader

import com.akrippner.mon.core.domain.vocabulary.vocabularyForm
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.ui.DisplayToken
import com.akrippner.mon.core.ui.JmdictEntry
import com.akrippner.mon.core.ui.Sense
import com.akrippner.mon.core.ui.TokenState

/**
 * Sudachi's part-of-speech names for punctuation (補助記号), other symbols (記号) and whitespace (空白).
 */
private val NON_WORD_POS = setOf("補助記号", "記号", "空白")

/** Characters that end a line in a whitespace token. */
private val LINE_BREAKS = charArrayOf('\n', '\r', ' ', ' ')

/**
 * Whether a tokenizer pack can exist for this language. Only the Japanese Sudachi pack does today
 * (see AGENTS.md); add a language here when its first tokenizer pack is published.
 */
internal fun Language.hasTokenizerPack(): Boolean = this == Language.JAPANESE

/** Punctuation and whitespace are drawn but cannot be looked up. */
internal fun MonToken.isPunctuation(): Boolean = partOfSpeech.firstOrNull() in NON_WORD_POS

/** [saved] marks the token as a saved word, drawn highlighted. Punctuation is never saved. */
internal fun MonToken.toDisplayToken(saved: Boolean = false): DisplayToken =
  DisplayToken(
    surface = surface,
    reading = furigana(surface, pronunciation),
    dictionaryForm = dictionaryForm,
    state =
      when {
        isPunctuation() -> TokenState.Punctuation
        saved -> TokenState.Annotated
        else -> TokenState.Default
      },
  )

/**
 * Groups the tokens into paragraphs. A whitespace token with a line break ends a paragraph and is
 * not drawn; a [ReaderToken] keeps the index of its token in [tokens]. Blank paragraphs are left
 * out. Tokens whose vocabulary form is in [savedForms] are drawn as saved words.
 */
internal fun buildParagraphs(
  tokens: List<MonToken>,
  savedForms: Set<String> = emptySet(),
): List<List<ReaderToken>> {
  val paragraphs = mutableListOf<List<ReaderToken>>()
  var current = mutableListOf<ReaderToken>()

  fun endParagraph() {
    if (current.any { it.display.surface.isNotBlank() }) paragraphs += current
    current = mutableListOf()
  }

  tokens.forEachIndexed { index, token ->
    if (token.surface.isBlank() && token.surface.any { it in LINE_BREAKS }) {
      endParagraph()
    } else {
      current += ReaderToken(index, token.toDisplayToken(token.vocabularyForm() in savedForms))
    }
  }
  endParagraph()
  return paragraphs
}

/**
 * The furigana to show above [surface], or an empty string if it needs none (it has no kanji, or
 * there is no reading).
 *
 * [pronunciation] is the tokenizer's reading in katakana; furigana is hiragana. Kana that the
 * surface already shows at its start or end (okurigana, like べ in 食べ) are cut from the reading, so
 * the furigana is for the kanji only: 食べ shows た. The furigana is drawn centered over the whole
 * word, so it is not aligned to individual kanji.
 *
 * The whole reading is shown when the surface and the reading do not line up that way, or when kana
 * sit between kanji (引き出し): then there is no single kanji part to cut to.
 */
internal fun furigana(surface: String, pronunciation: String): String {
  val units = surface.codePointUnits()
  if (units.none(::isKanji)) return ""
  var reading = katakanaToHiragana(pronunciation)
  if (reading.isBlank()) return ""
  if (units.dropWhile { !isKanji(it) }.dropLastWhile { !isKanji(it) }.any { !isKanji(it) }) {
    return reading
  }

  val trailingKana = units.takeLastWhile { !isKanji(it) }.joinToString("")
  for (length in trailingKana.length downTo 1) {
    val suffix = katakanaToHiragana(trailingKana.takeLast(length))
    if (reading.length > length && reading.endsWith(suffix)) {
      reading = reading.dropLast(length)
      break
    }
  }

  val leadingKana = units.takeWhile { !isKanji(it) }.joinToString("")
  for (length in leadingKana.length downTo 1) {
    val prefix = katakanaToHiragana(leadingKana.take(length))
    if (reading.length > length && reading.startsWith(prefix)) {
      reading = reading.drop(length)
      break
    }
  }
  return reading
}

/** The code points of the text, each as a string of one or two chars (a surrogate pair). */
private fun String.codePointUnits(): List<String> {
  val text = this
  val units = mutableListOf<String>()
  var i = 0
  while (i < text.length) {
    val pair = text[i].isHighSurrogate() && i + 1 < text.length && text[i + 1].isLowSurrogate()
    val end = if (pair) i + 2 else i + 1
    units += text.substring(i, end)
    i = end
  }
  return units
}

/** Whether [unit], one code point from [codePointUnits], is a kanji. */
private fun isKanji(unit: String): Boolean {
  if (unit.length == 2) {
    val code = 0x10000 + ((unit[0].code - 0xD800) shl 10) + (unit[1].code - 0xDC00)
    return code in 0x20000..0x2FA1F || // extensions B to F and the compatibility supplement
      code in 0x30000..0x323AF // extensions G and H
  }
  val char = unit[0]
  return char in '一'..'鿿' || // CJK unified ideographs
    char in '㐀'..'䶿' || // extension A
    char in '豈'..'﫿' || // compatibility ideographs
    char == '々' ||
    char == '〆'
}

private fun katakanaToHiragana(text: String): String =
  text.map { if (it in 'ァ'..'ヶ') (it.code - 0x60).toChar() else it }.joinToString(separator = "")

/** Maps a dictionary entry to what the sheet shows. Null if the entry has no headword. */
internal fun DictionaryEntry.toJmdictEntry(): JmdictEntry? {
  val headword = headwords.firstOrNull() ?: return null
  val entryPartOfSpeech = labels.partOfSpeech()
  return JmdictEntry(
    surface = headword.text,
    reading = headword.reading.orEmpty(),
    jlptLevel = null, // The dictionary models carry no JLPT level.
    senses =
      senses.map { sense ->
        Sense(
          posLabel = sense.labels.partOfSpeech() ?: entryPartOfSpeech,
          glosses = sense.glosses,
          usageNote = sense.labels.usageNote(),
        )
      },
  )
}

private fun List<Label>.partOfSpeech(): String? =
  filterIsInstance<Label.PartOfSpeech>().joinToString(", ") { it.text }.ifEmpty { null }

private fun List<Label>.usageNote(): String? =
  mapNotNull {
      when (it) {
        is Label.Register -> it.text
        is Label.Domain -> it.text
        is Label.Other -> it.text
        else -> null
      }
    }
    .joinToString(", ")
    .ifEmpty { null }
