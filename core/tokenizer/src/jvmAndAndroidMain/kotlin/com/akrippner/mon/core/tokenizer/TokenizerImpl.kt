package com.akrippner.mon.core.tokenizer

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.Tokenizer as SudachiTokenizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tokenizes Japanese with Sudachi. The dictionary is loaded on the first [tokenize] call, so
 * creating this class is cheap and the slow load happens on [dispatcher], not on the caller's
 * thread.
 */
class TokenizerImpl(
  loadDictionary: () -> Dictionary,
  override val id: String = "sudachi-bundled",
  private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : Tokenizer {

  override val language = Language.JAPANESE

  private val dictionary by lazy(loadDictionary)

  override suspend fun tokenize(text: String): List<MonToken> =
    withContext(dispatcher) {
      dictionary.create().tokenize(SudachiTokenizer.SplitMode.A, text).map { morpheme ->
        MonToken(
          surface = morpheme.surface(),
          dictionaryForm = morpheme.dictionaryForm(),
          pronunciation = morpheme.readingForm(),
          partOfSpeech = morpheme.partOfSpeech(),
        )
      }
    }
}
