package com.akrippner.mon.core.tokenizer

import com.akrippner.mon.core.model.data.MonToken
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.Tokenizer as SudachiTokenizer

/**
 * Tokenizes with Sudachi. The dictionary is loaded on the first [tokenize] call, so creating this
 * class is cheap and the slow load happens on the caller's (background) thread.
 */
class TokenizerImpl(loadDictionary: () -> Dictionary) : Tokenizer {

  private val dictionary by lazy(loadDictionary)

  override fun tokenize(text: String): List<MonToken> {
    return dictionary.create().tokenize(SudachiTokenizer.SplitMode.A, text).map { morpheme ->
      MonToken(
        surface = morpheme.surface(),
        dictionaryForm = morpheme.dictionaryForm(),
        pronunciation = morpheme.readingForm(),
        partOfSpeech = morpheme.partOfSpeech(),
      )
    }
  }
}
