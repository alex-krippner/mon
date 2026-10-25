package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.tokenizer.Token
import com.akrippner.mon.core.tokenizer.Tokenizer

data class FakeToken(
  override val text: String,
  override val dictionaryForm: String = text,
  override val readingForm: String = text,
  override val partOfSpeech: List<String> = emptyList(),
) : Token

class FakeTokenizer(private val tokens: List<Token> = emptyList()) : Tokenizer {
  val tokenizedTexts = mutableListOf<String>()

  override fun tokenize(text: String): List<Token> {
    tokenizedTexts += text
    return tokens
  }
}
