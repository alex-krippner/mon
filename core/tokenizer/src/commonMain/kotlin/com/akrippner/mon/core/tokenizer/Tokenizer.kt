package com.akrippner.mon.core.tokenizer

interface Tokenizer {
  fun tokenize(text: String): List<Token>
}
