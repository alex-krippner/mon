package com.akrippner.mon.core.tokenizer

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.packs.CatalogError
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackClosedException
import com.akrippner.mon.core.packs.PackManager
import com.akrippner.mon.core.packs.PackReader
import com.akrippner.mon.core.packs.PackState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class PackTokenizerProviderTest {
  private val tokens = listOf(MonToken("私", "私", "わたし", listOf("代名詞")))

  private fun pack(id: String, sha: String = "sha-$id") =
    CatalogPack(
      id = id,
      name = id,
      sourceLanguage = "ja",
      glossLanguage = "ja",
      format = "fake",
      schema = 1,
      version = "1",
      url = "https://example.test/$id.gz",
      sizeBytes = 0,
      unpackedSizeBytes = 0,
      sha256 = sha,
      attribution = "attribution",
    )

  private inner class FakeTokenizer(
    id: String,
    override val language: Language = Language.JAPANESE,
    private val closed: Boolean = false,
    private val failure: Throwable? = null,
    version: String = "1",
  ) : Tokenizer, PackReader {
    override val pack = pack(id)
    override val id = "$id@$version"

    override suspend fun tokenize(text: String): List<MonToken> {
      if (closed) throw PackClosedException()
      failure?.let { throw it }
      return tokens
    }
  }

  private inner class NotATokenizer(id: String) : PackReader {
    override val pack = pack(id)
  }

  private class FakeManager(var readers: List<PackReader>) : PackManager {
    var readersCalls = 0
    private val state = MutableStateFlow<List<Pack>>(emptyList())
    override val packs: StateFlow<List<Pack>> = state

    fun setPacks(vararg packs: Pack) {
      state.value = packs.toList()
    }

    override suspend fun refreshCatalog(): CatalogError? = null

    override suspend fun install(packId: String) {}

    override suspend fun remove(packId: String) {}

    override suspend fun readers(): List<PackReader> {
      readersCalls++
      return readers
    }
  }

  @Test
  fun offersTheTokenizerOfTheLanguage() = runTest {
    val manager =
      FakeManager(
        listOf(
          NotATokenizer("notes"),
          FakeTokenizer("german", Language.GERMAN),
          FakeTokenizer("sudachi-core"),
        )
      )

    val tokenizer = PackTokenizerProvider(manager).tokenizerFor(Language.JAPANESE)

    assertEquals("sudachi-core@1", tokenizer?.id)
    assertEquals(tokens, tokenizer?.tokenize("私"))
  }

  @Test
  fun noTokenizerWhenNothingIsInstalledForTheLanguage() = runTest {
    val manager = FakeManager(listOf(NotATokenizer("notes")))

    assertNull(PackTokenizerProvider(manager).tokenizerFor(Language.JAPANESE))
  }

  @Test
  fun aClosedReaderOfTheSameVersionIsReplacedAndTheCallRepeated() = runTest {
    val manager = FakeManager(listOf(FakeTokenizer("sudachi-core", closed = true)))
    val tokenizer = assertNotNull(PackTokenizerProvider(manager).tokenizerFor(Language.JAPANESE))
    manager.readers = listOf(FakeTokenizer("sudachi-core"))

    assertEquals(tokens, tokenizer.tokenize("私"))
  }

  @Test
  fun aReaderClosedByAnUpdateToANewVersionIsUnavailable() = runTest {
    val manager = FakeManager(listOf(FakeTokenizer("sudachi-core", closed = true)))
    val tokenizer = assertNotNull(PackTokenizerProvider(manager).tokenizerFor(Language.JAPANESE))
    manager.readers = listOf(FakeTokenizer("sudachi-core", version = "2"))

    // The tokens would come from version 2, but the reading would record version 1.
    assertFailsWith<TokenizerUnavailableException> { tokenizer.tokenize("私") }
  }

  @Test
  fun aReaderClosedByARemovalIsUnavailable() = runTest {
    val manager = FakeManager(listOf(FakeTokenizer("sudachi-core", closed = true)))
    val tokenizer = assertNotNull(PackTokenizerProvider(manager).tokenizerFor(Language.JAPANESE))
    manager.readers = emptyList()

    assertFailsWith<TokenizerUnavailableException> { tokenizer.tokenize("私") }
  }

  @Test
  fun givesUpAfterAFewAttemptsWhenTheReaderIsClosedEveryTime() = runTest {
    val manager = FakeManager(listOf(FakeTokenizer("sudachi-core", closed = true)))
    val tokenizer = assertNotNull(PackTokenizerProvider(manager).tokenizerFor(Language.JAPANESE))

    assertFailsWith<TokenizerUnavailableException> { tokenizer.tokenize("私") }
    assertEquals(3, manager.readersCalls) // once to find it, twice more to replace it
  }

  @Test
  fun otherFailuresAreNotHidden() = runTest {
    val broken = FakeTokenizer("sudachi-core", failure = IllegalStateException("damaged file"))
    val tokenizer =
      assertNotNull(
        PackTokenizerProvider(FakeManager(listOf(broken))).tokenizerFor(Language.JAPANESE)
      )

    assertFailsWith<IllegalStateException> { tokenizer.tokenize("私") }
  }

  @Test
  fun changesEmitsWhenATokenizerPackIsInstalledUpdatedOrRemoved() = runTest {
    val manager = FakeManager(emptyList())
    val emitted = mutableListOf<Unit>()
    val job =
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        PackTokenizerProvider(manager).changes.toList(emitted)
      }
    val base = pack("sudachi-core")
    assertEquals(0, emitted.size) // collecting is not a change

    manager.setPacks(Pack(base, PackState.NotInstalled))
    manager.setPacks(Pack(base, PackState.Downloading(0.5f)))
    manager.setPacks(Pack(base, PackState.Downloading(0.9f)))
    assertEquals(0, emitted.size) // listed or downloading for the first time: nothing installed

    manager.readers = listOf(FakeTokenizer("sudachi-core"))
    manager.setPacks(Pack(base, PackState.Installed))
    assertEquals(1, emitted.size) // installed

    val newer = pack("sudachi-core", sha = "newer")
    manager.setPacks(Pack(newer, PackState.UpdateAvailable(base)))
    assertEquals(1, emitted.size) // the old version is still the installed one

    manager.readers = listOf(FakeTokenizer("sudachi-core", version = "2"))
    manager.setPacks(Pack(newer, PackState.Installed))
    assertEquals(2, emitted.size) // updated

    val notes = pack("notes")
    manager.readers = manager.readers + NotATokenizer("notes")
    manager.setPacks(Pack(newer, PackState.Installed), Pack(notes, PackState.Installed))
    assertEquals(2, emitted.size) // another kind of pack is no change

    manager.readers = emptyList()
    manager.setPacks(Pack(newer, PackState.NotInstalled), Pack(notes, PackState.NotInstalled))
    assertEquals(3, emitted.size) // removed
    job.cancel()
  }
}
