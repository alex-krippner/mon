package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.DictionaryUnavailableException
import com.akrippner.mon.core.domain.fake.FakeDictionary
import com.akrippner.mon.core.domain.fake.FakeDictionaryProvider
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
    LookupWordUseCase(FakeDictionaryProvider(dictionaries.toList()))
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
  fun anUnavailableDictionaryIsSkipped() = runTest {
    val unavailable = FakeDictionary("gone", failure = DictionaryUnavailableException("replaced"))
    val working = FakeDictionary("ok", entries = listOf(entry("1", "ok")))

    assertEquals(
      LookupResult.Found(listOf(entry("1", "ok"))),
      lookup(dictionaryForm, unavailable, working),
    )
  }

  @Test
  fun aDictionaryRemovedDuringTheLookupGivesNoDictionaryInstalled() = runTest {
    val unavailable =
      FakeDictionary(failure = DictionaryUnavailableException("no longer installed"))
    val provider =
      object : DictionaryProvider {
        private var calls = 0

        override suspend fun dictionariesFor(source: Language, gloss: Language) =
          if (calls++ == 0) listOf(unavailable) else emptyList()
      }

    assertEquals(
      LookupResult.NoDictionaryInstalled,
      LookupWordUseCase(provider).execute(dictionaryForm, Language.JAPANESE, Language.ENGLISH),
    )
  }

  @Test
  fun onlyUnavailableDictionariesGiveNotFound() = runTest {
    val unavailable = FakeDictionary(failure = DictionaryUnavailableException("replaced"))

    assertEquals(LookupResult.NotFound, lookup(dictionaryForm, unavailable))
  }
}
