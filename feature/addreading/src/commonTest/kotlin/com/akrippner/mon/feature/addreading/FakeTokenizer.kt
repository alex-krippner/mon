package com.akrippner.mon.feature.addreading

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.tokenizer.Tokenizer
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeTokenizer : Tokenizer {
  override val language = Language.JAPANESE
  override val id = "fake-tokenizer"

  override suspend fun tokenize(text: String): List<MonToken> = emptyList()
}

class FakeTokenizerProvider : TokenizerProvider {
  private val tokenizer = FakeTokenizer()

  override suspend fun tokenizerFor(language: Language): Tokenizer? =
    tokenizer.takeIf { it.language == language }

  override val changes: Flow<Unit> = emptyFlow()
}
