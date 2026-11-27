package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.domain.fake.FakeReadingRepository
import com.akrippner.mon.core.domain.fake.FakeVocabularyRepository
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.Sense
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class SaveWordUseCaseTest {
  private val readings = FakeReadingRepository()
  private val vocabulary = FakeVocabularyRepository()
  private val useCase = SaveWordUseCase(readings, vocabulary)

  // 私が鎌倉で出会っ た。
  private val tokens =
    listOf(
      MonToken("私", "私", "ワタシ", listOf("代名詞")),
      MonToken("が", "が", "ガ", listOf("助詞")),
      MonToken("鎌倉", "鎌倉", "カマクラ", listOf("名詞", "固有名詞")),
      MonToken("で", "で", "デ", listOf("助詞")),
      MonToken("出会っ", "出会う", "デアッ", listOf("動詞")),
      MonToken("た", "た", "タ", listOf("助動詞")),
      MonToken("。", "。", "。", listOf("補助記号")),
    )

  init {
    readings.readings["r1"] =
      Reading(
        id = "r1",
        title = "こころ",
        text = tokens.joinToString("") { it.surface },
        translation = "",
        sourceLanguage = Language.JAPANESE,
        translationLanguage = Language.ENGLISH,
        tokens = tokens,
        isTokenized = true,
        createdAt = 0,
      )
  }

  private val source = DictionarySource("jmdict-eng", "JMdict", "EDRDG", "1")

  private fun entry(
    id: String = "jmdict-eng:1163460",
    headwords: List<Headword> = listOf(Headword("出会う", "であう")),
    glosses: List<List<String>> =
      listOf(listOf("to meet", "to encounter"), listOf("to come across")),
  ) = DictionaryEntry(id, source, headwords, glosses.map { Sense(it) })

  @Test
  fun savesTheWordWithItsSentenceAndADictionarySnapshot() = runTest {
    val word = useCase.execute("r1", 4, entry())!!

    assertEquals("r1", word.readingId)
    assertEquals("出会う", word.dictionaryForm)
    assertEquals("出会っ", word.surface)
    assertEquals("であう", word.kana)
    assertEquals("to meet; to encounter", word.gloss)
    assertEquals(1_163_460L, word.jmdictSeq)
    assertNull(word.jlpt)
    assertEquals("私が鎌倉で出会った。", word.sentence.text)
    assertEquals(5, word.sentence.wordStart)
    assertEquals(8, word.sentence.wordEnd)
    assertEquals(0, word.sentence.textOffset)
  }

  @Test
  fun savingTheSameWordAgain_returnsTheSavedWordWithoutAddingAnother() = runTest {
    val first = useCase.execute("r1", 4, entry())
    val second = useCase.execute("r1", 4, entry())

    assertEquals(first, second)
    assertEquals(1, vocabulary.words.value.size)
  }

  @Test
  fun aKanaOnlyWordWithoutAReading_usesItsOwnSpelling() = runTest {
    val word = useCase.execute("r1", 3, entry(id = "x", headwords = listOf(Headword("で"))))!!

    assertEquals("で", word.kana)
    assertNull(word.jmdictSeq)
  }

  @Test
  fun anEntryWithoutAReading_fallsBackToTheTokenizersReadingAsHiragana() = runTest {
    val word = useCase.execute("r1", 2, entry(headwords = listOf(Headword("鎌倉"))))!!

    assertEquals("かまくら", word.kana)
  }

  @Test
  fun theHeadwordThatMatchesTheDictionaryForm_givesTheKana() = runTest {
    val headwords = listOf(Headword("出逢う", "であう"), Headword("出会う", "であう"))

    val word = useCase.execute("r1", 4, entry(headwords = headwords))!!

    assertEquals("であう", word.kana)
  }

  @Test
  fun anEntryWithoutSenses_savesAnEmptyGloss() = runTest {
    val word = useCase.execute("r1", 4, entry(glosses = emptyList()))!!

    assertEquals("", word.gloss)
  }

  @Test
  fun aMissingReadingOrToken_savesNothing() = runTest {
    assertNull(useCase.execute("missing", 4, entry()))
    assertNull(useCase.execute("r1", 99, entry()))
    assertEquals(emptyList(), vocabulary.saved)
  }

  @Test
  fun removeAndRestore_bringTheWordBackWithItsNote() = runTest {
    val word = useCase.execute("r1", 4, entry())!!
    SetSavedWordNoteUseCase(vocabulary).execute(word.id, "  memory  ")

    val removed = RemoveSavedWordUseCase(vocabulary).execute(word.id)!!
    assertEquals(emptyList(), vocabulary.words.value)

    RestoreSavedWordUseCase(vocabulary).execute(removed)
    assertEquals("memory", vocabulary.words.value.single().note)
    assertNull(RemoveSavedWordUseCase(vocabulary).execute("missing"))
  }
}
