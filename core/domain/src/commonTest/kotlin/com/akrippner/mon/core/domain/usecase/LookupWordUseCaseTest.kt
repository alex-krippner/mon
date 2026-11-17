package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.dictionary.DictionaryClosedException
import com.akrippner.mon.core.domain.fake.FakeDictionary
import com.akrippner.mon.core.domain.fake.FakeDictionaryManager
import com.akrippner.mon.core.domain.model.LookupResult
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Sense
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest

class LookupWordUseCaseTest {
  private fun entry(id: String, source: String = "fake") =
    DictionaryEntry(
      id = id,
      source = DictionarySource(source, source, "attribution", "1"),
      headwords = listOf(Headword("食べる", "たべる")),
      senses = listOf(Sense(listOf("to eat"))),
    )

  private val dictionaryForm =
    MonToken("食べる", dictionaryForm = "食べる", pronunciation = "タベル", partOfSpeech = listOf("動詞"))
  private val inflected =
    MonToken("食べた", dictionaryForm = "食べる", pronunciation = "タベタ", partOfSpeech = listOf("動詞"))

  private suspend fun lookup(token: MonToken, vararg dictionaries: FakeDictionary) =
    LookupWordUseCase(FakeDictionaryManager(dictionaries.toList()))
      .execute(token, Language.JAPANESE, Language.ENGLISH)

  @Test
  fun reportsWhenNoDictionaryIsInstalled() = runTest {
    assertEquals(LookupResult.NoDictionaryInstalled, lookup(dictionaryForm))
  }

  @Test
  fun ignoresDictionariesForOtherLanguagePairs() = runTest {
    val reverse =
      FakeDictionary(
        sourceLanguage = Language.ENGLISH,
        glossLanguage = Language.JAPANESE,
        entries = listOf(entry("a")),
      )

    assertEquals(LookupResult.NoDictionaryInstalled, lookup(dictionaryForm, reverse))
    assertEquals(emptyList(), reverse.queries)
  }

  @Test
  fun returnsTheEntriesOfTheMatchingDictionary() = runTest {
    val dictionary = FakeDictionary(entries = listOf(entry("a")))

    assertEquals(LookupResult.Found(listOf(entry("a"))), lookup(dictionaryForm, dictionary))
  }

  @Test
  fun reportsNotFoundWhenTheDictionaryHasNothing() = runTest {
    assertEquals(LookupResult.NotFound, lookup(dictionaryForm, FakeDictionary()))
  }

  @Test
  fun queriesWithLemmaSurfaceAndReadingForADictionaryFormToken() = runTest {
    val dictionary = FakeDictionary()

    lookup(dictionaryForm, dictionary)

    assertEquals(listOf(LookupQuery("食べる", surface = "食べる", reading = "タベル")), dictionary.queries)
  }

  @Test
  fun leavesOutTheReadingOfAnInflectedSurface() = runTest {
    val dictionary = FakeDictionary()

    lookup(inflected, dictionary)

    assertEquals(listOf(LookupQuery("食べる", surface = "食べた", reading = null)), dictionary.queries)
  }

  @Test
  fun mergesTheEntriesOfAllMatchingDictionariesInOrder() = runTest {
    val first = FakeDictionary("one", entries = listOf(entry("1", "one")))
    val second = FakeDictionary("two", entries = listOf(entry("2", "two"), entry("3", "two")))

    assertEquals(
      LookupResult.Found(listOf(entry("1", "one"), entry("2", "two"), entry("3", "two"))),
      lookup(dictionaryForm, first, second),
    )
  }

  @Test
  fun aFailingDictionaryDoesNotHideTheOthers() = runTest {
    val broken = FakeDictionary("broken", failure = IllegalStateException("damaged file"))
    val working = FakeDictionary("ok", entries = listOf(entry("1", "ok")))

    assertEquals(
      LookupResult.Found(listOf(entry("1", "ok"))),
      lookup(dictionaryForm, broken, working),
    )
  }

  @Test
  fun onlyFailingDictionariesGiveNotFound() = runTest {
    val broken = FakeDictionary(failure = IllegalStateException("damaged file"))

    assertEquals(LookupResult.NotFound, lookup(dictionaryForm, broken))
  }

  @Test
  fun cancellationIsNotSwallowed() = runTest {
    val cancelled = FakeDictionary(failure = CancellationException("cancelled"))

    assertFailsWith<CancellationException> { lookup(dictionaryForm, cancelled) }
  }

  @Test
  fun aReaderClosedByAnUpdateIsReplacedAndTheLookupRepeated() = runTest {
    val closed = FakeDictionary(failure = DictionaryClosedException())
    val reopened = FakeDictionary(entries = listOf(entry("a")))
    val manager = FakeDictionaryManager(listOf(closed)).apply { afterFirstCall = listOf(reopened) }

    val result =
      LookupWordUseCase(manager).execute(dictionaryForm, Language.JAPANESE, Language.ENGLISH)

    assertEquals(LookupResult.Found(listOf(entry("a"))), result)
    assertEquals(2, manager.readersCalls)
  }

  @Test
  fun aReaderClosedByARemovalGivesNoDictionaryInstalled() = runTest {
    val closed = FakeDictionary(failure = DictionaryClosedException())
    val manager = FakeDictionaryManager(listOf(closed)).apply { afterFirstCall = emptyList() }

    val result =
      LookupWordUseCase(manager).execute(dictionaryForm, Language.JAPANESE, Language.ENGLISH)

    assertEquals(LookupResult.NoDictionaryInstalled, result)
  }

  @Test
  fun givesUpAfterAFewAttemptsWhenTheReaderIsClosedEveryTime() = runTest {
    val closed = FakeDictionary(failure = DictionaryClosedException())
    val manager = FakeDictionaryManager(listOf(closed))

    val result =
      LookupWordUseCase(manager).execute(dictionaryForm, Language.JAPANESE, Language.ENGLISH)

    assertEquals(LookupResult.NotFound, result)
    assertEquals(3, manager.readersCalls)
  }
}
