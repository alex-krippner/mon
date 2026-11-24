package com.akrippner.mon.feature.reader

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.ui.DisplayToken
import com.akrippner.mon.core.ui.lookup.LookupUiState

@Immutable
data class ReaderState(
  val title: String = "",
  val isLoading: Boolean = true,
  val error: ReaderError? = null,
  /** True while the reading waits for a tokenizer: only [plainText] can be shown. */
  val isPending: Boolean = false,
  /**
   * True if a pending reading can still get tokens, because a tokenizer pack exists for its
   * language. Only meaningful while [isPending].
   */
  val tokenizerMayArrive: Boolean = false,
  val plainText: String = "",
  /** The tokens of the reading, one list per paragraph. Empty while [isPending]. */
  val paragraphs: List<List<ReaderToken>> = emptyList(),
  val showFurigana: Boolean = true,
  /** The tapped word and what the dictionary says about it; null when no sheet is open. */
  val selection: ReaderSelection? = null,
)

/** A token to draw. [index] is its position in the reading's tokens. */
@Immutable data class ReaderToken(val index: Int, val display: DisplayToken)

@Immutable
data class ReaderSelection(val index: Int, val token: DisplayToken, val lookup: LookupUiState)

enum class ReaderError {
  NotFound,
  LoadFailed,
}

sealed interface ReaderIntent {
  data object Back : ReaderIntent

  data object ToggleFurigana : ReaderIntent

  data class TokenTapped(val index: Int) : ReaderIntent

  data object DismissLookup : ReaderIntent

  data object RetryLookup : ReaderIntent

  data object OpenDictionaries : ReaderIntent
}

sealed interface ReaderEffect {
  data object Close : ReaderEffect

  data object OpenDictionaries : ReaderEffect
}
