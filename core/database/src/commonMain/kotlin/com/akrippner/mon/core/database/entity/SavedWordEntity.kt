package com.akrippner.mon.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A word the user saved from a reading, with the sentence it was saved from and a snapshot of its
 * dictionary data. A word is saved at most once per reading.
 *
 * @property id Unique identifier, a UUID generated on the device.
 * @property readingId The [ReadingEntity] it was saved from. The word goes with the reading.
 * @property dictionaryForm The tokenizer's dictionary form. Unique together with [readingId].
 * @property surface The word as it appears in the sentence.
 * @property kana The reading in kana.
 * @property gloss The first sense of the entry, at save time.
 * @property jlpt Name of a `JlptLevel`, or null.
 * @property jmdictSeq The entry's number in JMdict, or null.
 * @property sentenceText The whole sentence the word was saved from.
 * @property wordStart UTF-16 offset of the word inside [sentenceText].
 * @property wordEnd UTF-16 offset after the word inside [sentenceText].
 * @property textOffset UTF-16 offset of the sentence inside the reading's text.
 * @property note The user's note, or null.
 * @property savedAt Time of saving in milliseconds since epoch.
 */
@Entity(
  tableName = "saved_word",
  foreignKeys =
    [
      ForeignKey(
        entity = ReadingEntity::class,
        parentColumns = ["id"],
        childColumns = ["readingId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index(value = ["readingId", "dictionaryForm"], unique = true)],
)
data class SavedWordEntity(
  @PrimaryKey val id: String,
  val readingId: String,
  val dictionaryForm: String,
  val surface: String,
  val kana: String,
  val gloss: String,
  val jlpt: String?,
  val jmdictSeq: Long?,
  val sentenceText: String,
  val wordStart: Int,
  val wordEnd: Int,
  val textOffset: Int,
  val note: String?,
  val savedAt: Long,
)
