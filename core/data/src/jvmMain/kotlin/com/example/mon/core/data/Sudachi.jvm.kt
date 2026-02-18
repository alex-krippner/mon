package com.example.mon.core.data

import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.DictionaryFactory

actual fun createSudachiDictionary(): Dictionary {
    val config = Config.fromClasspath("sudachi.json")
    return DictionaryFactory().create(config)
}
