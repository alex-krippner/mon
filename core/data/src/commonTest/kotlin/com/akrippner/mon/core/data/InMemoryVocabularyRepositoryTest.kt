package com.akrippner.mon.core.data

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MAX_NOTE_LENGTH
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SentenceRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class InMemoryVocabularyRepositoryTest {
  private val readings = InMemoryReadingRepository()
  private val vocabulary = InMemoryVocabularyRepository(readings)

  private suspend fun newReading(title: String) =
    readings.addReading(title, "text", "", Language.JAPANESE, Language.ENGLISH, analysis = null)

  private fun newWord(readingId: String, form: String, offset: Int = 0) =
    NewSavedWord(
      readingId = readingId,
      dictionaryForm = form,
      surface = form,
      kana = form,
      gloss = "gloss of $form",
      jlpt = null,
      jmdictSeq = null,
      sentence = SentenceRef(text = form, wordStart = 0, wordEnd = form.length, textOffset = offset),
    )

  @Test
  fun savingTheSameWordTwiceKeepsTheFirst() = runTest {
    val reading = newReading("r")

    val first = vocabulary.save(newWord(reading, "猫"))
    val second = vocabulary.save(newWord(reading, "猫"))

    assertEquals(first, second)
    assertEquals(1, vocabulary.observeCount(reading).first())
  }

  @Test
  fun wordsOfAReadingComeInTextOrder() = runTest {
    val reading = newReading("r")
    vocabulary.save(newWord(reading, "犬", offset = 10))
    vocabulary.save(newWord(reading, "猫", offset = 2))

    assertEquals(listOf("猫", "犬"), vocabulary.observeForReading(reading).first().map { it.surface })
  }

  @Test
  fun groupsFollowTheReadingsAndSkipReadingsWithoutWords() = runTest {
    val older = newReading("older")
    val newer = newReading("newer")
    newReading("empty")
    vocabulary.save(newWord(older, "猫"))
    vocabulary.save(newWord(newer, "犬"))
    vocabulary.save(newWord(newer, "鳥"))

    val groups = vocabulary.observeGroups().first()

    assertEquals(listOf("newer", "older"), groups.map { it.title })
    // Newest saved word first.
    assertEquals(listOf("鳥", "犬"), groups.first().words.map { it.surface })
  }

  @Test
  fun removedWordComesBackWithRestore() = runTest {
    val reading = newReading("r")
    val saved = vocabulary.save(newWord(reading, "猫"))

    assertEquals(saved, vocabulary.remove(saved.id))
    assertNull(vocabulary.observeWord(saved.id).first())
    assertNull(vocabulary.remove(saved.id))

    vocabulary.restore(saved)

    assertEquals(saved, vocabulary.observeWord(saved.id).first())
  }

  @Test
  fun noteIsTrimmedCutAndClearedWhenBlank() = runTest {
    val reading = newReading("r")
    val saved = vocabulary.save(newWord(reading, "猫"))

    vocabulary.setNote(saved.id, "  my note  ")
    assertEquals("my note", vocabulary.observeWord(saved.id).first()?.note)

    vocabulary.setNote(saved.id, "x".repeat(MAX_NOTE_LENGTH + 20))
    assertEquals(MAX_NOTE_LENGTH, vocabulary.observeWord(saved.id).first()?.note?.length)

    vocabulary.setNote(saved.id, "   ")
    assertNull(vocabulary.observeWord(saved.id).first()?.note)
  }
}
