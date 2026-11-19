package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.tokenizer.TokenizerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Keeps readings without tokens up to date: tokenizes them once at start and again whenever the
 * available tokenizers change (a pack was installed, say). A killed app catches up on the next
 * start, so no background job is needed while tokenizing is local.
 */
class PendingTokenization(
  private val tokenizePending: TokenizePendingReadingsUseCase,
  private val tokenizers: TokenizerProvider,
) {
  /**
   * Runs one tokenization at a time: while one runs, further triggers collapse into a single
   * follow-up run. It lives as long as [scope].
   */
  fun start(scope: CoroutineScope): Job =
    scope.launch {
      tokenizers.changes.onStart { emit(Unit) }.conflate().collect { tokenizePending.execute() }
    }
}
