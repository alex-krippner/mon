package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.tokenizer.Tokenizer

class FakeTokenizer(private val tokens: List<MonToken> = emptyList()) : Tokenizer {
  val tokenizedTexts = mutableListOf<String>()

  override fun tokenize(text: String): List<MonToken> {
    tokenizedTexts += text
    return tokens
  }
}
