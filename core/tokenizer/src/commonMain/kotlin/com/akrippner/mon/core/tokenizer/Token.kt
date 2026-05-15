package com.akrippner.mon.core.tokenizer

interface Token {
  val dictionaryForm: String
  val partOfSpeech: List<String>
  val readingForm: String
  val text: String
}
