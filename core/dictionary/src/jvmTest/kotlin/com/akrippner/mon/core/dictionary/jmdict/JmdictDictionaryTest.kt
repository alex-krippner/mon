package com.akrippner.mon.core.dictionary.jmdict

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import com.akrippner.mon.core.model.data.*
import com.akrippner.mon.core.packs.*
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.PackClosedException
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class JmdictDictionaryTest {
  private val directory = Files.createTempDirectory("jmdict-test")
  private val source = DictionarySource("jmdict", "JMdict", "attribution", "test")

  @AfterTest
  fun cleanUp() {
    directory.toFile().deleteRecursively()
  }

  /** Writes [records] to a fresh file whose headwords are in [language] and returns its path. */
  private fun writeFile(
    language: Language,
    vararg records: JmdictRecord,
    beforeFinish: (JmdictWriter) -> Unit = {},
  ): String {
    val path = Files.createTempFile(directory, "dict", ".db").toString()
    JmdictWriter(path, language).use { writer ->
      records.forEach(writer::insert)
      beforeFinish(writer)
      writer.finish()
    }
    return path
  }

  /**
   * Writes [records] to a fresh file, runs [block] against it, then closes it. [writtenFor] is the
   * language the writer gets and [openedAs] the one the reader gets; they only differ in a test.
   */
  private suspend fun withDictionary(
    vararg records: JmdictRecord,
    writtenFor: Language = Language.JAPANESE,
    openedAs: Language = writtenFor,
    block: suspend (JmdictDictionary) -> Unit,
  ) {
    val path = writeFile(writtenFor, *records)
    val dictionary = JmdictDictionary(path, source, openedAs, Language.ENGLISH)
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
      assertFailsWith<PackClosedException> { dictionary.lookup(LookupQuery("食べる")) }
    }
  }

  private val haus = record(10, "Haus", null, "house")

  /** What a file says about its keys: the meta row and the stored keys. */
  private fun keysOf(path: String): Pair<String?, Set<String>> {
    val connection = BundledSQLiteDriver().open(path, SQLITE_OPEN_READONLY)
    try {
      val keys =
        connection.prepare("SELECT key FROM lookup_key").use { statement ->
          buildSet { while (statement.step()) add(statement.getText(0)) }
        }
      return connection.meta(KEY_NORMALIZATION) to keys
    } finally {
      connection.close()
    }
  }

  private fun packFor(sourceLanguage: String) =
    CatalogPack(
      id = "test",
      name = "Test",
      sourceLanguage = sourceLanguage,
      glossLanguage = "de",
      format = JmdictSchema.FORMAT,
      schema = JmdictSchema.VERSION,
      version = "1",
      url = "https://example.test/test.db.gz",
      sizeBytes = 0,
      unpackedSizeBytes = 0,
      sha256 = "",
      attribution = "test",
    )

  @Test
  fun latinKeysIgnoreCaseAndUnicodeForm() = runTest {
    val decomposed = record(11, "Häuser", null, "houses") // "a" + combining diaeresis
    withDictionary(haus, decomposed, writtenFor = Language.ENGLISH) { dictionary ->
      assertEquals("jmdict:10", dictionary.lookup(LookupQuery("haus")).single().id)
      assertEquals("jmdict:10", dictionary.lookup(LookupQuery("HAUS")).single().id)
      assertEquals("jmdict:11", dictionary.lookup(LookupQuery("Häuser")).single().id)
      // The headword shown is the original spelling, not the key.
      assertEquals("Haus", dictionary.lookup(LookupQuery("haus")).single().headwords.single().text)
      // The surface fallback is normalized too.
      val bySurface = dictionary.lookup(LookupQuery(lemma = "nothing", surface = "HAUS"))
      assertEquals("jmdict:10", bySurface.single().id)
    }
  }

  @Test
  fun headwordsThatNormalizeToTheSameKeyAreFoundOnce() = runTest {
    val both =
      JmdictRecord(
        12,
        false,
        listOf(Sense(listOf("house"))),
        listOf(Headword("Haus"), Headword("haus")),
      )
    withDictionary(both, writtenFor = Language.ENGLISH) { dictionary ->
      assertEquals(1, dictionary.lookup(LookupQuery("haus")).size)
    }
  }

  @Test
  fun japaneseFileKeepsItsKeysAndHasNoRule() = runTest {
    val shirt = record(13, "Tシャツ", "ティーシャツ", "T-shirt")
    val (rule, keys) = keysOf(writeFile(Language.JAPANESE, shirt))
    assertNull(rule)
    assertEquals(setOf("Tシャツ", "ティーシャツ"), keys)

    withDictionary(shirt) { dictionary ->
      assertEquals(1, dictionary.lookup(LookupQuery("Tシャツ")).size)
      assertTrue(dictionary.lookup(LookupQuery("tシャツ")).isEmpty())
    }
  }

  @Test
  fun latinFileRecordsItsRule() {
    val (rule, keys) = keysOf(writeFile(Language.ENGLISH, haus))
    assertEquals("nfc-lower", rule)
    assertEquals(setOf("haus"), keys)
  }

  @Test
  fun readerFollowsTheFileNotItsLanguage() = runTest {
    withDictionary(haus, writtenFor = Language.ENGLISH, openedAs = Language.JAPANESE) { dictionary
      ->
      assertEquals(1, dictionary.lookup(LookupQuery("HAUS")).size)
    }
  }

  @Test
  fun fileWithAnUnknownKeyRuleIsNotValid() {
    val known = writeFile(Language.ENGLISH, haus)
    val japanese = writeFile(Language.JAPANESE, taberu)
    val unknown =
      writeFile(Language.ENGLISH, haus) { it.putMeta(KEY_NORMALIZATION, "a-future-rule") }

    assertTrue(isValidJmdictFile(File(known), packFor("en")))
    assertTrue(isValidJmdictFile(File(japanese), packFor("ja")))
    assertFalse(isValidJmdictFile(File(unknown), packFor("en")))
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
