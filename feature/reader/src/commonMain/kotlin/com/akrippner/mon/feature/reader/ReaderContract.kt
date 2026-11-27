package com.akrippner.mon.feature.reader

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.model.data.SavedWord
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
  /** How many words of this reading are saved; the header button is hidden at 0. */
  val savedCount: Int = 0,
  /** True if the reader was opened from the vocabulary: its header button is active. */
  val openedFromVocabulary: Boolean = false,
  /** The saved word "Show in reading" points at; null if none, or once the user moved on. */
  val focus: ReaderFocus? = null,
)

/**
 * The word to scroll to when the reader opens from the vocabulary. [tokenIndex] is its position in
 * the reading's tokens. [outlined] is true until the user scrolls or taps.
 */
@Immutable data class ReaderFocus(val tokenIndex: Int, val outlined: Boolean = true)

/** A token to draw. [index] is its position in the reading's tokens. */
@Immutable data class ReaderToken(val index: Int, val display: DisplayToken)

@Immutable
data class ReaderSelection(
  val index: Int,
  val token: DisplayToken,
  val lookup: LookupUiState,
  /** True if the lookup found an entry, so the word can be saved. */
  val canSave: Boolean = false,
  /** The saved word for the tapped token, or null if it is not saved. */
  val saved: SavedWord? = null,
  /** The note as typed; it is stored when the field loses focus and when the sheet closes. */
  val noteDraft: String = "",
)

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

  data object ToggleSave : ReaderIntent

  data class NoteChanged(val note: String) : ReaderIntent

  data object NoteCommitted : ReaderIntent

  data class UndoRemove(val word: SavedWord) : ReaderIntent

  /** The header button with the number of saved words. */
  data object OpenVocabulary : ReaderIntent

  /** The user scrolled away from the focused word: its outline is not needed any more. */
  data object ClearOutline : ReaderIntent
}

sealed interface ReaderEffect {
  data object Close : ReaderEffect

  data object OpenDictionaries : ReaderEffect

  /** A word was removed from the vocabulary; the screen offers Undo. */
  data class ShowRemoved(val word: SavedWord) : ReaderEffect

  /** Open the list of this reading's saved words. */
  data class OpenVocabulary(val readingId: String) : ReaderEffect

  /** Saving a word failed. */
  data object SaveFailed : ReaderEffect
}
