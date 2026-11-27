package com.akrippner.mon.core.data.mapper

import com.akrippner.mon.core.data.internal.toStoredNote
import com.akrippner.mon.core.database.entity.SavedWordEntity
import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.model.data.MAX_NOTE_LENGTH
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SentenceRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedWordMappersTest {
  private fun entity(jlpt: String? = "N5", note: String? = "my note") =
    SavedWordEntity(
      id = "w1",
      readingId = "r1",
      dictionaryForm = "出会う",
      surface = "出会っ",
      kana = "であう",
      gloss = "to meet; to encounter",
      jlpt = jlpt,
      jmdictSeq = 1_163_460L,
      sentenceText = "私が鎌倉でその人に出会ったのは夏のことであった。",
      wordStart = 9,
      wordEnd = 12,
      textOffset = 40,
      note = note,
      savedAt = 7,
    )

  @Test
  fun entity_roundTripsThroughTheDomainModel() {
    val entity = entity()

    val word = entity.toDomain()

    assertEquals("出会う", word.dictionaryForm)
    assertEquals(JlptLevel.N5, word.jlpt)
    assertEquals(SentenceRef(entity.sentenceText, 9, 12, 40), word.sentence)
    assertEquals(49, word.sentence.wordOffset)
    assertEquals(entity, word.toEntity())
  }

  @Test
  fun anUnknownJlptName_showsNoBadgeInsteadOfFailing() {
    assertNull(entity(jlpt = "N0").toDomain().jlpt)
    assertNull(entity(jlpt = null).toDomain().jlpt)
  }

  @Test
  fun aNewWord_getsItsIdAndTime_andNoNote() {
    val word =
      NewSavedWord(
        readingId = "r1",
        dictionaryForm = "夏",
        surface = "夏",
        kana = "なつ",
        gloss = "summer",
        jlpt = JlptLevel.N5,
        jmdictSeq = 1,
        sentence = SentenceRef("夏だ。", 0, 1, 3),
      )

    val stored = word.toEntity(id = "new-id", savedAt = 99)

    assertEquals("new-id", stored.id)
    assertEquals(99, stored.savedAt)
    assertNull(stored.note)
    assertEquals("N5", stored.jlpt)
    assertEquals(3, stored.textOffset)
  }

  @Test
  fun aNote_isTrimmed_andABlankNoteIsNull() {
    assertEquals("memory themes", "  memory themes \n".toStoredNote())
    assertNull("   ".toStoredNote())
    assertNull("".toStoredNote())
    assertNull(null.toStoredNote())
  }

  @Test
  fun aLongNote_isCutToTheLimit() {
    val note = "あ".repeat(MAX_NOTE_LENGTH + 20)

    assertEquals(MAX_NOTE_LENGTH, note.toStoredNote()?.length)
  }
}
