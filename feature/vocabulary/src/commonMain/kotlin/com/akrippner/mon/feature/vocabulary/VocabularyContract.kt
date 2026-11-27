package com.akrippner.mon.feature.vocabulary

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.model.data.SavedWord

/**
 * The state of the Vocabulary tab (all readings, grouped) and of the list of one reading. The two
 * differ only in what [sections] holds: the tab has a section per reading, the list of one reading
 * has exactly one, with every word.
 */
@Immutable
data class VocabularyState(
  /** True for the list of one reading, which shows its title in the bar. */
  val forOneReading: Boolean = false,
  /** The title of the reading; only for [forOneReading]. */
  val readingTitle: String = "",
  val isLoading: Boolean = true,
  val error: VocabularyError? = null,
  val query: String = "",
  val levels: Set<JlptLevel> = emptySet(),
  /** False while no listed word has a JLPT level: there is nothing to filter by then. */
  val showLevelFilter: Boolean = false,
  val sections: List<VocabularySection> = emptyList(),
  /** How many words there are, before the search and the filter. */
  val totalWords: Int = 0,
  /** The word whose sheet is open, with the note as typed. */
  val openWord: OpenWord? = null,
) {
  val isFiltering: Boolean
    get() = query.isNotBlank() || levels.isNotEmpty()

  /** Nothing is saved at all. */
  val isEmpty: Boolean
    get() = !isLoading && error == null && totalWords == 0

  /** Words are saved, but none matches the search and filter. */
  val hasNoMatches: Boolean
    get() = !isLoading && totalWords > 0 && sections.isEmpty()
}

/** One reading's words as listed. */
@Immutable
data class VocabularySection(
  val readingId: String,
  val title: String,
  /** How many words the reading has, or how many match while searching. */
  val wordCount: Int,
  val words: List<SavedWord>,
  /** How many more words the reading has than [words] shows; 0 while searching. */
  val moreCount: Int,
)

@Immutable data class OpenWord(val word: SavedWord, val readingTitle: String, val noteDraft: String)

enum class VocabularyError {
  LoadFailed
}

sealed interface VocabularyIntent {
  data object Back : VocabularyIntent

  data class QueryChanged(val query: String) : VocabularyIntent

  data class LevelToggled(val level: JlptLevel) : VocabularyIntent

  data object ClearLevels : VocabularyIntent

  /** "See all" and "+ N more": open the list of one reading. */
  data class OpenReading(val readingId: String) : VocabularyIntent

  data class WordTapped(val wordId: String) : VocabularyIntent

  data object DismissWord : VocabularyIntent

  data class NoteChanged(val note: String) : VocabularyIntent

  data object NoteCommitted : VocabularyIntent

  data object ShowInReading : VocabularyIntent

  data object Remove : VocabularyIntent

  data class UndoRemove(val word: SavedWord) : VocabularyIntent
}

sealed interface VocabularyEffect {
  data object Close : VocabularyEffect

  data class OpenReadingVocabulary(val readingId: String) : VocabularyEffect

  /** Open the reading at the sentence of the saved word. */
  data class ShowInReading(val readingId: String, val wordId: String) : VocabularyEffect

  /** A word was removed; the screen offers Undo. */
  data class ShowRemoved(val word: SavedWord) : VocabularyEffect
}
