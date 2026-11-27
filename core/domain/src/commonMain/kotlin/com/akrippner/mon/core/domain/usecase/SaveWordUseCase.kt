package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.domain.vocabulary.sentenceAround
import com.akrippner.mon.core.domain.vocabulary.vocabularyForm
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SavedWord

/**
 * Saves a word of a reading to the vocabulary, with the sentence it stands in and a snapshot of its
 * dictionary entry, so it still shows when the dictionary pack is removed.
 */
class SaveWordUseCase(
  private val readings: ReadingRepository,
  private val vocabulary: VocabularyRepository,
) {
  /**
   * Saves the token at [tokenIndex] of the reading, described by [entry]. If the reading already
   * has the word, nothing changes and the saved word is returned. Returns null if the reading or
   * the token is gone.
   */
  suspend fun execute(readingId: String, tokenIndex: Int, entry: DictionaryEntry): SavedWord? {
    val reading = readings.getReading(readingId) ?: return null
    val token = reading.tokens.getOrNull(tokenIndex) ?: return null
    val sentence = sentenceAround(reading.tokens, tokenIndex) ?: return null
    val dictionaryForm = token.vocabularyForm()
    val headword =
      entry.headwords.firstOrNull { it.text == dictionaryForm } ?: entry.headwords.firstOrNull()

    return vocabulary.save(
      NewSavedWord(
        readingId = readingId,
        dictionaryForm = dictionaryForm,
        surface = token.surface,
        kana =
          headword?.reading
            ?: headword?.text?.takeIf { it.all(::isKana) }
            ?: katakanaToHiragana(token.pronunciation),
        gloss = entry.senses.firstOrNull()?.glosses?.joinToString("; ").orEmpty(),
        // The dictionaries carry no JLPT level yet.
        jlpt = null,
        // The id is "<pack id>:<JMdict entry number>", e.g. "jmdict-eng:1358280".
        jmdictSeq = entry.id.substringAfterLast(':').toLongOrNull(),
        sentence = sentence,
      )
    )
  }
}

private fun isKana(char: Char): Boolean = char in 'ぁ'..'ゟ' || char in '゠'..'ヿ'

/** The tokenizer's readings are katakana; the vocabulary shows kana as hiragana. */
private fun katakanaToHiragana(text: String): String =
  text.map { if (it in 'ァ'..'ヶ') (it.code - 0x60).toChar() else it }.joinToString("")
