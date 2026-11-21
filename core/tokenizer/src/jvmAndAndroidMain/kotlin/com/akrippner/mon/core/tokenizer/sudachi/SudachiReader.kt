package com.akrippner.mon.core.tokenizer.sudachi

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.CloseableReader
import com.akrippner.mon.core.packs.PackClosedException
import com.akrippner.mon.core.tokenizer.Tokenizer
import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer.SplitMode
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Tokenizes Japanese with the Sudachi dictionary in [file]. The dictionary is loaded on the first
 * [tokenize] call, on [dispatcher], so creating a reader is cheap.
 *
 * Tokenizing and closing exclude each other: the dictionary is memory-mapped, so closing it while a
 * text is being tokenized could crash. A closed reader throws [PackClosedException] instead of
 * reopening the file behind the manager's back, which would block replacing or deleting it.
 */
internal class SudachiReader(
  private val file: File,
  override val pack: CatalogPack,
  private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : Tokenizer, CloseableReader {

  override val language: Language = Language.fromTag(pack.sourceLanguage)

  override val id: String = "${pack.id}@${pack.version}"

  private val mutex = Mutex()
  private var dictionary: Dictionary? = null
  private var closed = false

  override suspend fun tokenize(text: String): List<MonToken> =
    withContext(dispatcher) {
      mutex.withLock {
        if (closed) throw PackClosedException()
        val tokenizer = (dictionary ?: load().also { dictionary = it }).create()
        tokenizer.tokenize(SplitMode.A, text).map { morpheme ->
          MonToken(
            surface = morpheme.surface(),
            dictionaryForm = morpheme.dictionaryForm(),
            pronunciation = morpheme.readingForm(),
            partOfSpeech = morpheme.partOfSpeech(),
          )
        }
      }
    }

  /**
   * The default configuration of the Sudachi jar, with this pack's file as the system dictionary.
   */
  private fun load(): Dictionary {
    val config = Config.fromClasspath("sudachi.json").systemDictionary(file.toPath())
    return DictionaryFactory().create(config)
  }

  override suspend fun close() {
    mutex.withLock {
      closed = true
      dictionary?.close()
      dictionary = null
    }
  }
}
