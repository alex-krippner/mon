package com.akrippner.mon.core.tokenizer

import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.Tokenizer as SudachiTokenizer

/**
 * Tokenizes with Sudachi. The dictionary is loaded on the first [tokenize] call, so creating this
 * class is cheap and the slow load happens on the caller's (background) thread.
 */
class TokenizerImpl(loadDictionary: () -> Dictionary) : Tokenizer {

  private val dictionary by lazy(loadDictionary)

  override fun tokenize(text: String): List<Token> {
    return dictionary.create().tokenize(SudachiTokenizer.SplitMode.A, text).map { morpheme ->
      TokenImpl(
        dictionaryForm = morpheme.dictionaryForm(),
        partOfSpeech = morpheme.partOfSpeech(),
        readingForm = morpheme.readingForm(),
        text = morpheme.surface(),
      )
    }
  }
}
