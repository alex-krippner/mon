package com.akrippner.mon.core.dictionary.jmdict

import com.akrippner.mon.core.dictionary.DictionaryClosedException
import com.akrippner.mon.core.model.data.*
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class JmdictDictionaryTest {
  private val directory = Files.createTempDirectory("jmdict-test")
  private val source = DictionarySource("jmdict", "JMdict", "attribution", "test")

  @AfterTest
  fun cleanUp() {
    directory.toFile().deleteRecursively()
  }

  /** Writes [records] to a fresh file, runs [block] against it, then closes it. */
  private suspend fun withDictionary(
    vararg records: JmdictRecord,
    block: suspend (JmdictDictionary) -> Unit,
  ) {
    val path = Files.createTempFile(directory, "dict", ".db").toString()
    JmdictWriter(path).use { writer ->
      records.forEach(writer::insert)
      writer.finish()
    }
    val dictionary = JmdictDictionary(path, source, Language.JAPANESE, Language.ENGLISH)
    try {
      block(dictionary)
    } finally {
      dictionary.close()
    }
  }

  private fun record(
    id: Long,
    text: String,
    reading: String?,
    gloss: String,
    common: Boolean = false,
    labels: List<Label> = emptyList(),
  ) =
    JmdictRecord(id, common, listOf(Sense(listOf(gloss))), listOf(Headword(text, reading)), labels)

  private val taberu =
    JmdictRecord(
      id = 1358280,
      common = true,
      senses = listOf(Sense(listOf("to eat"), listOf(Label.PartOfSpeech("Ichidan verb")))),
      headwords = listOf(Headword("食べる", "たべる"), Headword("喰べる", "たべる")),
    )

  private val hi = record(1, "日", "ひ", "day; sun", common = true)
  private val nichi = record(2, "日", "にち", "Sunday")

  @Test
  fun findsEntryByLemma() = runTest {
    withDictionary(taberu) { dictionary ->
      val result = dictionary.lookup(LookupQuery("食べる")).single()
      assertEquals("jmdict:1358280", result.id)
      assertEquals(source, result.source)
      assertEquals(taberu.headwords, result.headwords)
      assertEquals(taberu.senses, result.senses)
      assertEquals(listOf(Label.Frequency("common")), result.labels)
    }
  }

  @Test
  fun fallsBackToSurface() = runTest {
    withDictionary(taberu) { dictionary ->
      val result = dictionary.lookup(LookupQuery(lemma = "食べれる", surface = "食べる")).single()
      assertEquals("jmdict:1358280", result.id)
    }
  }

  @Test
  fun findsEntryByReading() = runTest {
    withDictionary(taberu) { dictionary ->
      val result = dictionary.lookup(LookupQuery("たべる")).single()
      assertEquals("jmdict:1358280", result.id)
    }
  }

  @Test
  fun findsEntryByAlternativeSpelling() = runTest {
    withDictionary(taberu) { dictionary ->
      val result = dictionary.lookup(LookupQuery("喰べる")).single()
      assertEquals("jmdict:1358280", result.id)
    }
  }

  @Test
  fun unknownWordReturnsEmpty() = runTest {
    withDictionary(taberu) { dictionary ->
      assertTrue(dictionary.lookup(LookupQuery("存在しない")).isEmpty())
    }
  }

  @Test
  fun readingMatchRanksFirst() = runTest {
    withDictionary(hi, nichi) { dictionary ->
      val ids = dictionary.lookup(LookupQuery("日", reading = "ニチ")).map { it.id }
      assertEquals(listOf("jmdict:2", "jmdict:1"), ids)
    }
  }

  @Test
  fun commonRanksFirstWithoutReading() = runTest {
    withDictionary(hi, nichi) { dictionary ->
      val ids = dictionary.lookup(LookupQuery("日")).map { it.id }
      assertEquals(listOf("jmdict:1", "jmdict:2"), ids)
    }
  }

  @Test
  fun limitsResults() = runTest {
    val records = (1L..15L).map { record(it, "日", "ひ", "gloss $it") }
    withDictionary(*records.toTypedArray()) { dictionary ->
      val ids = dictionary.lookup(LookupQuery("日")).map { it.id }
      assertEquals((1..10).map { "jmdict:$it" }, ids)
    }
  }

  @Test
  fun labelsRoundTrip() = runTest {
    val labels =
      listOf(
        Label.PartOfSpeech("noun"),
        Label.Gender(GrammaticalGender.NEUTRAL),
        Label.Register("colloquial"),
        Label.Domain("medicine"),
        Label.Frequency("news"),
        Label.Other("misc"),
      )
    val tagged =
      JmdictRecord(
        id = 5,
        common = false,
        senses = listOf(Sense(listOf("gloss"), labels)),
        headwords = listOf(Headword("語")),
        labels = labels,
      )
    withDictionary(tagged) { dictionary ->
      val result = dictionary.lookup(LookupQuery("語")).single()
      assertEquals(labels, result.labels)
      assertEquals(labels, result.senses.single().labels)
    }
  }

  @Test
  fun closedDictionaryThrowsInsteadOfReopeningOrReportingNothing() = runTest {
    withDictionary(taberu) { dictionary ->
      assertEquals(1, dictionary.lookup(LookupQuery("食べる")).size)
      dictionary.close()
      assertFailsWith<DictionaryClosedException> { dictionary.lookup(LookupQuery("食べる")) }
    }
  }

  @Test
  fun unknownLabelKindBecomesOther() {
    assertEquals(Label.Other("x"), LabelDto("future", "x").toLabel())
  }

  @Test
  fun unknownGenderBecomesOther() {
    assertEquals(Label.Other("ALIEN"), LabelDto("gender", "ALIEN").toLabel())
  }

  @Test
  fun convertsKatakanaToHiragana() {
    assertEquals("たべる", "タベル".toHiragana())
    assertEquals("こーひー", "コーヒー".toHiragana())
    assertEquals("食べる", "食べる".toHiragana())
  }
}
