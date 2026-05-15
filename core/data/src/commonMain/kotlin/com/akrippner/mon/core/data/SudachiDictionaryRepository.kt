package com.akrippner.mon.core.data

import com.akrippner.mon.core.data.model.DictionaryRepository
import com.akrippner.mon.core.model.data.MyDictionary
import com.akrippner.mon.core.model.data.MyToken
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.Tokenizer

class SudachiDictionaryRepository : DictionaryRepository {
  private val sudachi: Dictionary by lazy { createSudachiDictionary() }

  override fun getDictionary(): MyDictionary {
    return object : MyDictionary {
      override fun tokenize(text: String): List<MyToken> {
        return sudachi.create().tokenize(Tokenizer.SplitMode.A, text).map { morpheme ->
          MyToken(
            surface = morpheme.surface(),
            partOfSpeech = morpheme.partOfSpeech(),
            normalizedForm = morpheme.normalizedForm(),
            reading = morpheme.readingForm(),
            dictForm = morpheme.dictionaryForm(),
          )
        }
      }
    }
  }
}
