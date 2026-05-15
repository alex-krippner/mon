package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.tokenizer.Token
import com.akrippner.mon.core.tokenizer.Tokenizer

class TokenizeTextUseCase(private val tokenizer: Tokenizer) {
  fun execute(text: String): List<Token> {

    return tokenizer.tokenize(text)
  }
}
