package com.akrippner.mon.core.model.data

/** JLPT level of a word. N5 is the easiest. */
enum class JlptLevel {
  N5,
  N4,
  N3,
  N2,
  N1,
}

/**
 * The sentence a word was saved from, copied at save time so it stays readable when the reading
 * changes.
 *
 * @property text The whole sentence, without surrounding whitespace.
 * @property wordStart UTF-16 offset of the saved word inside [text].
 * @property wordEnd UTF-16 offset after the saved word inside [text].
 * @property textOffset UTF-16 offset of the start of [text] inside the reading's text.
 */
data class SentenceRef(
  val text: String,
  val wordStart: Int,
  val wordEnd: Int,
  val textOffset: Int,
) {
  /** Offset of the saved word inside the reading's text. */
  val wordOffset: Int
    get() = textOffset + wordStart
}

/**
 * A word the user saved from a reading. There is one per word per reading, identified by the
 * reading and the word's [dictionaryForm]. It keeps a snapshot of its dictionary data, so it still
 * shows when the dictionary pack is removed.
 *
 * @property id Unique identifier, a UUID generated on the device.
 * @property readingId The reading it was saved from.
 * @property dictionaryForm The tokenizer's dictionary form, e.g. 出会う.
 * @property surface The word as it appears in the sentence, e.g. 出会っ.
 * @property kana The reading in kana, e.g. であう.
 * @property gloss The first sense of the entry, at save time.
 * @property jlpt Null when unknown: no badge is shown.
 * @property jmdictSeq The entry's number in JMdict, to refresh the snapshot when a pack is
 *   installed.
 * @property note The user's note, or null.
 * @property savedAt Time of saving, in milliseconds since epoch.
 */
data class SavedWord(
  val id: String,
  val readingId: String,
  val dictionaryForm: String,
  val surface: String,
  val kana: String,
  val gloss: String,
  val jlpt: JlptLevel?,
  val jmdictSeq: Long?,
  val sentence: SentenceRef,
  val note: String?,
  val savedAt: Long,
)

/** A [SavedWord] that is not stored yet: the repository gives it an id and a time. */
data class NewSavedWord(
  val readingId: String,
  val dictionaryForm: String,
  val surface: String,
  val kana: String,
  val gloss: String,
  val jlpt: JlptLevel?,
  val jmdictSeq: Long?,
  val sentence: SentenceRef,
)

/** The saved words of one reading, newest first. */
data class VocabularyGroup(val readingId: String, val title: String, val words: List<SavedWord>)

/** The most characters a note can have. */
const val MAX_NOTE_LENGTH = 500
