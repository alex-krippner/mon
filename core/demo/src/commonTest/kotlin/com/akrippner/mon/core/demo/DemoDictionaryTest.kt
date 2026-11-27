package com.akrippner.mon.core.demo

import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.GrammaticalGender
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Sense
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DemoDictionaryTest {
  private val source = DictionarySource("jmdict-eng", "JMdict", "CC BY-SA", "1")
  private val cat =
    DictionaryEntry(
      id = "jmdict:1467640",
      source = source,
      headwords = listOf(Headword("猫", "ねこ")),
      senses =
        listOf(
          Sense(
            glosses = listOf("cat"),
            labels = listOf(Label.PartOfSpeech("noun"), Label.Gender(GrammaticalGender.NEUTRAL)),
          )
        ),
      labels = listOf(Label.Frequency("common"), Label.Register("colloquial")),
    )
  private val query = LookupQuery(lemma = "猫", surface = "猫", reading = "ネコ")

  private val seed =
    DemoSeed(
      dictionary =
        DemoDictionaryInfo(
          "jmdict-eng",
          "JMdict",
          "CC BY-SA",
          "1",
          sourceLanguage = "ja",
          glossLanguage = "en",
        ),
      readings =
        listOf(
          DemoReading(
            id = "demo-1",
            title = "Cat",
            text = "猫",
            translation = "cat",
            sourceLanguage = "ja",
            translationLanguage = "en",
            tokens = listOf(DemoToken.from(MonToken("猫", "猫", "ネコ", listOf("名詞")))),
            createdAt = 1,
          )
        ),
      lookups = listOf(DemoLookup.from(query, listOf(cat))),
    )

  @Test
  fun seedSurvivesAJsonRoundTrip() {
    val json = DemoSeedJson.encodeToString(DemoSeed.serializer(), seed)

    assertEquals(seed, DemoSeedJson.decodeFromString(DemoSeed.serializer(), json))
  }

  @Test
  fun readingsComeBackTokenized() {
    val reading = seed.toReadings().single()

    assertTrue(reading.isTokenized)
    assertEquals(Language.JAPANESE, reading.sourceLanguage)
    assertEquals(listOf("猫"), reading.tokens.map { it.surface })
  }

  @Test
  fun lookupReturnsTheStoredEntryWithItsLabels() = runTest {
    val entries = DemoDictionary(seed).lookup(query)

    assertEquals(listOf(cat), entries)
  }

  @Test
  fun lookupFallsBackToTheLemma() = runTest {
    val entries =
      DemoDictionary(seed).lookup(LookupQuery(lemma = "猫", surface = "ねこ", reading = null))

    assertEquals(listOf(cat), entries)
  }

  @Test
  fun unknownWordIsNotFound() = runTest {
    assertTrue(DemoDictionary(seed).lookup(LookupQuery(lemma = "犬")).isEmpty())
  }

  @Test
  fun providerOnlyAnswersItsLanguagePair() = runTest {
    val provider = DemoDictionaryProvider(seed)

    assertEquals(1, provider.dictionariesFor(Language.JAPANESE, Language.ENGLISH).size)
    assertTrue(provider.dictionariesFor(Language.JAPANESE, Language.GERMAN).isEmpty())
  }
}
