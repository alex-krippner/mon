package com.akrippner.mon.core.tokenizer

import com.akrippner.mon.core.model.data.MonToken

interface Tokenizer {
  fun tokenize(text: String): List<MonToken>
}
