package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.tokenizer.Tokenizer
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeTokenizer(
  private val tokens: List<MonToken> = emptyList(),
  override val language: Language = Language.JAPANESE,
  override val id: String = "fake-tokenizer",
) : Tokenizer {
  val tokenizedTexts = mutableListOf<String>()

  override suspend fun tokenize(text: String): List<MonToken> {
    tokenizedTexts += text
    return tokens
  }
}

/** Offers [tokenizer] for its own language only, or nothing at all when it is null. */
class FakeTokenizerProvider(private val tokenizer: Tokenizer? = null) : TokenizerProvider {
  override suspend fun tokenizerFor(language: Language): Tokenizer? =
    tokenizer?.takeIf { it.language == language }

  override val changes: Flow<Unit> = emptyFlow()
}
