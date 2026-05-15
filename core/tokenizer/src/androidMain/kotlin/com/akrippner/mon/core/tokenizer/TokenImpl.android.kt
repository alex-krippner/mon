package com.akrippner.mon.core.tokenizer

data class TokenImpl(
  override val dictionaryForm: String,
  override val partOfSpeech: List<String>,
  override val readingForm: String,
  override val text: String,
) : Token
