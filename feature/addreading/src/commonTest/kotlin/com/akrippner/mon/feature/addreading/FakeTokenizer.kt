package com.akrippner.mon.feature.addreading

import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.tokenizer.Tokenizer

class FakeTokenizer : Tokenizer {
  override fun tokenize(text: String): List<MonToken> = emptyList()
}
