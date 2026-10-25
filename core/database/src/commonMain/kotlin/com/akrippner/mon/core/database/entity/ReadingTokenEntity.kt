package com.akrippner.mon.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey

/**
 * Represents a single analyzed token (word or punctuation) within a [ReadingEntity].
 *
 * Each token captures linguistic data such as its part of speech, dictionary form, and phonetic
 * reading, allowing for interactive features like furigana display and dictionary lookups.
 *
 * @property readingId The ID of the [ReadingEntity] that contains this token.
 * @property tokenIndex The zero-based position of this token in the original text, used for
 *   reconstructing the sentence order.
 * @property surface The raw text of the token as it appears in the source material.
 * @property pronunciation The phonetic representation of the token (e.g., in hiragana or katakana).
 * @property dictionaryForm The canonical or lemma form of the word, useful for dictionary lookups.
 * @property partOfSpeech A list of grammatical classifications for the token (e.g.,
 *   ["noun", "proper"]).
 */
@Entity(
  tableName = "reading_token",
  primaryKeys = ["readingId", "tokenIndex"],
  foreignKeys =
    [
      ForeignKey(
        entity = ReadingEntity::class,
        parentColumns = ["id"],
        childColumns = ["readingId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
)
data class ReadingTokenEntity(
  val readingId: String,
  val tokenIndex: Int,
  val surface: String,
  val pronunciation: String,
  val dictionaryForm: String,
  val partOfSpeech: List<String>,
)
