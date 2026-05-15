package com.akrippner.mon.core.tokenizer

import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.Tokenizer as SudachiTokenizer

class TokenizerImpl(val dictionary: Dictionary) : Tokenizer {

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
