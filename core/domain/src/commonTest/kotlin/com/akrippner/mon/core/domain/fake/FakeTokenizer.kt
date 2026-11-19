package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.tokenizer.Tokenizer
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeTokenizer(
  private val tokens: List<MonToken> = emptyList(),
  override val language: Language = Language.JAPANESE,
  override val id: String = "fake-tokenizer",
  private val failure: Throwable? = null,
) : Tokenizer {
  val tokenizedTexts = mutableListOf<String>()

  override suspend fun tokenize(text: String): List<MonToken> {
    tokenizedTexts += text
    failure?.let { throw it }
    return tokens
  }
}

/**
 * Offers [tokenizer] for its own language only. [tokenizer] can be changed, and [changed] emits, as
 * when a pack is installed or removed.
 */
class FakeTokenizerProvider(var tokenizer: Tokenizer? = null) : TokenizerProvider {
  private val trigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  override suspend fun tokenizerFor(language: Language): Tokenizer? =
    tokenizer?.takeIf { it.language == language }

  override val changes: Flow<Unit> = trigger

  fun changed() {
    check(trigger.tryEmit(Unit))
  }
}
