package com.akrippner.mon.core.tokenizer

import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.DictionaryFactory

fun createSudachiDictionary(): Dictionary {
  val config = Config.fromClasspath("sudachi.json")
  return DictionaryFactory().create(config)
}
