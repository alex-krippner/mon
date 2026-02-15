package com.example.mon.data

import com.example.mon.domain.DictionaryRepository
import com.example.mon.domain.model.MyDictionary
import com.example.mon.domain.model.MyToken
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.Tokenizer

class SudachiDictionaryRepository : DictionaryRepository {
    private val sudachi: Dictionary by lazy {
        createSudachiDictionary()
    }

    override fun getDictionary(): MyDictionary {
        return object : MyDictionary {
            override fun tokenize(text: String): List<MyToken> {
                return sudachi.create()
                    .tokenize(Tokenizer.SplitMode.C, text)
                    .map { morpheme ->
                        MyToken(
                            surface = morpheme.surface(),
                            partOfSpeech = morpheme.partOfSpeech(),
                            normalizedForm = morpheme.normalizedForm(),
                            reading = morpheme.readingForm()
                        )
                    }
            }
        }
    }
}
