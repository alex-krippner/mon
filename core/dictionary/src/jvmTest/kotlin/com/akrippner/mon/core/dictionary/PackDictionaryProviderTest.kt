package com.akrippner.mon.core.dictionary

import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.Sense
import com.akrippner.mon.core.packs.CatalogError
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackClosedException
import com.akrippner.mon.core.packs.PackManager
import com.akrippner.mon.core.packs.PackReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest

class PackDictionaryProviderTest {
  private val query = LookupQuery("食べる")

  private fun entry(id: String) =
    DictionaryEntry(
      id = id,
      source = DictionarySource("fake", "fake", "attribution", "1"),
      headwords = listOf(Headword("食べる", "たべる")),
      senses = listOf(Sense(listOf("to eat"))),
    )

  private suspend fun PackDictionaryProvider.single() =
    dictionariesFor(Language.JAPANESE, Language.ENGLISH).single()

  @Test
  fun offersOnlyTheDictionariesOfTheRequestedLanguagePair() = runTest {
    val reverse = FakeReader("reverse", Language.ENGLISH, Language.JAPANESE)
    val manager = FakeManager(listOf(FakeReader("fake"), reverse))

    val found = PackDictionaryProvider(manager).dictionariesFor(Language.JAPANESE, Language.ENGLISH)

    assertEquals(listOf("fake"), found.map { it.source.id })
  }

  @Test
  fun ignoresReadersThatCannotLookWordsUp() = runTest {
    val manager = FakeManager(listOf(NotADictionary("notes")))

    assertEquals(
      emptyList(),
      PackDictionaryProvider(manager).dictionariesFor(Language.JAPANESE, Language.ENGLISH),
    )
  }

  @Test
  fun aReaderClosedByAnUpdateIsReplacedAndTheLookupRepeated() = runTest {
    val manager = FakeManager(listOf(FakeReader("fake", closed = true)))
    val dictionary = PackDictionaryProvider(manager).single()
    manager.readers = listOf(FakeReader("fake", entries = listOf(entry("a"))))

    assertEquals(listOf(entry("a")), dictionary.lookup(query))
    assertEquals(2, manager.readersCalls) // once to find it, once after it was closed
  }

  @Test
  fun aReaderClosedByARemovalIsUnavailable() = runTest {
    val manager = FakeManager(listOf(FakeReader("fake", closed = true)))
    val dictionary = PackDictionaryProvider(manager).single()
    manager.readers = emptyList()

    assertFailsWith<DictionaryUnavailableException> { dictionary.lookup(query) }
  }

  @Test
  fun givesUpAfterAFewAttemptsWhenTheReaderIsClosedEveryTime() = runTest {
    val manager = FakeManager(listOf(FakeReader("fake", closed = true)))
    val dictionary = PackDictionaryProvider(manager).single()

    assertFailsWith<DictionaryUnavailableException> { dictionary.lookup(query) }
    assertEquals(3, manager.readersCalls) // once to find it, twice more to replace it
  }

  @Test
  fun otherFailuresAreNotHidden() = runTest {
    val broken = FakeReader("fake", failure = IllegalStateException("damaged file"))
    val dictionary = PackDictionaryProvider(FakeManager(listOf(broken))).single()

    assertFailsWith<IllegalStateException> { dictionary.lookup(query) }
  }

  private fun catalogPack(id: String, source: Language, gloss: Language) =
    CatalogPack(
      id = id,
      name = id,
      sourceLanguage = source.tag,
      glossLanguage = gloss.tag,
      format = "fake",
      schema = 1,
      version = "1",
      url = "https://example.test/$id.db.gz",
      sizeBytes = 0,
      unpackedSizeBytes = 0,
      sha256 = "",
      attribution = "attribution",
    )

  private inner class FakeReader(
    id: String,
    override val sourceLanguage: Language = Language.JAPANESE,
    override val glossLanguage: Language = Language.ENGLISH,
    private val closed: Boolean = false,
    private val entries: List<DictionaryEntry> = emptyList(),
    private val failure: Throwable? = null,
  ) : MonDictionary, PackReader {
    override val pack = catalogPack(id, sourceLanguage, glossLanguage)
    override val source = DictionarySource(id, id, "attribution", "1")

    override suspend fun lookup(query: LookupQuery): List<DictionaryEntry> {
      if (closed) throw PackClosedException()
      failure?.let { throw it }
      return entries
    }
  }

  private inner class NotADictionary(id: String) : PackReader {
    override val pack = catalogPack(id, Language.JAPANESE, Language.ENGLISH)
  }

  private class FakeManager(var readers: List<PackReader>) : PackManager {
    var readersCalls = 0
    override val packs: StateFlow<List<Pack>> = MutableStateFlow(emptyList())

    override suspend fun refreshCatalog(): CatalogError? = null

    override suspend fun install(packId: String) {}

    override suspend fun remove(packId: String) {}

    override suspend fun readers(): List<PackReader> {
      readersCalls++
      return readers
    }
  }
}
