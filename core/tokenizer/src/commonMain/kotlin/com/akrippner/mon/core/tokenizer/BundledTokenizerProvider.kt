package com.akrippner.mon.core.tokenizer

import com.akrippner.mon.core.model.data.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Offers one tokenizer that is always there, for its own language only. */
class BundledTokenizerProvider(private val tokenizer: Tokenizer) : TokenizerProvider {
  override suspend fun tokenizerFor(language: Language): Tokenizer? =
    tokenizer.takeIf { it.language == language }

  override val changes: Flow<Unit> = emptyFlow()
}
