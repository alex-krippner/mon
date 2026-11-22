package com.akrippner.mon.core.domain.usecase

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch

private val log = Logger.withTag("PendingTokenization")

private val RETRY_DELAY = 30.seconds

/**
 * Keeps readings without tokens up to date: tokenizes them once at start, again whenever the
 * available tokenizers change (a pack was installed, say) and whenever [request] is called. A
 * killed app catches up on the next start, so no background job is needed while tokenizing is
 * local.
 */
class PendingTokenization(
  private val tokenizePending: TokenizePendingReadingsUseCase,
  private val tokenizers: TokenizerProvider,
) {
  private val requests =
    MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  /** Asks for a run, e.g. after a reading was stored without tokens. Never blocks. */
  fun request() {
    requests.tryEmit(Unit)
  }

  /**
   * Runs one tokenization at a time: while one runs, further triggers collapse into a single
   * follow-up run. It lives as long as [scope].
   */
  fun start(scope: CoroutineScope): Job =
    scope.launch {
      // [TokenizerProvider.changes] emits when collected, which is the run at start.
      merge(retryingChanges(), requests).conflate().collect { runOnce() }
    }

  /**
   * [TokenizerProvider.changes] can fail (it reads the installed packs from disk). Collecting it
   * again after a pause keeps the loop alive. Collecting it again emits the current state, which
   * catches up on what was missed.
   */
  private fun retryingChanges(): Flow<Unit> =
    tokenizers.changes.retryWhen { cause, _ ->
      if (cause is CancellationException) return@retryWhen false
      log.e(cause) { "Watching the tokenizers failed; trying again in $RETRY_DELAY" }
      delay(RETRY_DELAY)
      true
    }

  /** One failed run must not end the collector: the next trigger tries again. */
  private suspend fun runOnce() {
    try {
      tokenizePending.execute()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      log.e(e) { "Tokenizing the pending readings failed; the next trigger tries again" }
    }
  }
}
