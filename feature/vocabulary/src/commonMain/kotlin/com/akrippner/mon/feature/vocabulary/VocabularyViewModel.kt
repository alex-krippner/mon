package com.akrippner.mon.feature.vocabulary

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.domain.usecase.RemoveSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.RestoreSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.SetSavedWordNoteUseCase
import com.akrippner.mon.core.model.data.MAX_NOTE_LENGTH
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val log = Logger.withTag("VocabularyViewModel")

/**
 * The saved words: of all readings, grouped by reading (the Vocabulary tab, [readingId] is null),
 * or of one reading.
 *
 * Tapping a word opens its sheet, where the note can be edited, the word can be removed (with Undo)
 * and the reading can be opened at the word's sentence.
 */
class VocabularyViewModel(
  private val readingId: String?,
  private val vocabulary: VocabularyRepository,
  private val readings: ReadingRepository,
  private val removeWord: RemoveSavedWordUseCase,
  private val restoreWord: RestoreSavedWordUseCase,
  private val setNote: SetSavedWordNoteUseCase,
) :
  BaseViewModel<VocabularyState, VocabularyIntent, VocabularyEffect>(
    initialState = VocabularyState(forOneReading = readingId != null)
  ) {

  private var groups: List<VocabularyGroup> = emptyList()

  /** The title of the single reading, from the library, so it shows even with no saved word. */
  private var readingTitle: String = ""

  init {
    vocabulary
      .observeGroups()
      .onEach { all ->
        groups = if (readingId == null) all else all.filter { it.readingId == readingId }
        refresh()
      }
      .catch { e ->
        log.e(e) { "Could not observe the vocabulary" }
        updateState { copy(isLoading = false, error = VocabularyError.LoadFailed) }
      }
      .launchIn(viewModelScope)

    if (readingId != null) {
      readings
        .observeReadingSummaries()
        .onEach { summaries ->
          readingTitle = summaries.firstOrNull { it.id == readingId }?.title.orEmpty()
          updateState { copy(readingTitle = this@VocabularyViewModel.readingTitle) }
        }
        .catch { e -> log.w(e) { "Could not observe the title of reading $readingId" } }
        .launchIn(viewModelScope)
    }
  }

  override fun onIntent(intent: VocabularyIntent) {
    when (intent) {
      VocabularyIntent.Back -> sendEffect(VocabularyEffect.Close)
      is VocabularyIntent.QueryChanged -> {
        updateState { copy(query = intent.query) }
        refresh()
      }
      is VocabularyIntent.LevelToggled -> {
        updateState {
          copy(
            levels = if (intent.level in levels) levels - intent.level else levels + intent.level
          )
        }
        refresh()
      }
      VocabularyIntent.ClearLevels -> {
        updateState { copy(levels = emptySet()) }
        refresh()
      }
      is VocabularyIntent.OpenReading ->
        sendEffect(VocabularyEffect.OpenReadingVocabulary(intent.readingId))
      is VocabularyIntent.WordTapped -> openWord(intent.wordId)
      VocabularyIntent.DismissWord -> {
        commitNote()
        updateState { copy(openWord = null) }
      }
      is VocabularyIntent.NoteChanged ->
        updateState {
          copy(openWord = openWord?.copy(noteDraft = intent.note.take(MAX_NOTE_LENGTH)))
        }
      VocabularyIntent.NoteCommitted -> commitNote()
      VocabularyIntent.ShowInReading -> showInReading()
      VocabularyIntent.Remove -> removeOpenWord()
      is VocabularyIntent.UndoRemove -> undoRemove(intent.word)
    }
  }

  private fun refresh() {
    updateState {
      val sections =
        buildSections(
          groups = groups,
          query = query,
          levels = levels,
          previewCount = if (forOneReading) null else TAB_PREVIEW_COUNT,
        )
      val allWords = groups.flatMap { it.words }
      copy(
        isLoading = false,
        error = null,
        sections = sections,
        totalWords = allWords.size,
        showLevelFilter = allWords.any { it.jlpt != null },
        // A removed word closes its sheet; a changed one (its note) keeps what is being typed.
        openWord =
          openWord?.let { open ->
            groups
              .firstNotNullOfOrNull { group ->
                group.words.firstOrNull { it.id == open.word.id }?.let { group to it }
              }
              ?.let { (group, word) -> open.copy(word = word, readingTitle = group.title) }
          },
      )
    }
  }

  private fun openWord(wordId: String) {
    for (group in groups) {
      val word = group.words.firstOrNull { it.id == wordId } ?: continue
      updateState {
        copy(openWord = OpenWord(word, readingTitle = group.title, noteDraft = word.note.orEmpty()))
      }
      return
    }
  }

  private fun showInReading() {
    val open = state.value.openWord ?: return
    commitNote()
    updateState { copy(openWord = null) }
    sendEffect(VocabularyEffect.ShowInReading(open.word.readingId, open.word.id))
    recordOpened(open.word.readingId)
  }

  private fun removeOpenWord() {
    val open = state.value.openWord ?: return
    viewModelScope.launch {
      try {
        // Store what was typed first, so Undo brings the word back with its note.
        if (open.noteDraft.toNote() != open.word.note) setNote.execute(open.word.id, open.noteDraft)
        removeWord.execute(open.word.id)?.let { sendEffect(VocabularyEffect.ShowRemoved(it)) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        log.w(e) { "Could not remove '${open.word.surface}'" }
      }
    }
  }

  private fun undoRemove(word: SavedWord) {
    viewModelScope.launch {
      try {
        restoreWord.execute(word)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        log.w(e) { "Could not restore '${word.surface}'" }
      }
    }
  }

  /** Stores the typed note of the open sheet, if it changed. */
  private fun commitNote() {
    val open = state.value.openWord ?: return
    if (open.noteDraft.toNote() == open.word.note) return
    viewModelScope.launch {
      try {
        setNote.execute(open.word.id, open.noteDraft)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        log.w(e) { "Could not store the note of '${open.word.surface}'" }
      }
    }
  }

  /** Opening a reading from here counts as opening it, like in the library. Best effort. */
  private fun recordOpened(readingId: String) {
    viewModelScope.launch {
      try {
        readings.recordReadingOpened(readingId)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        log.w(e) { "Could not record that reading $readingId was opened" }
      }
    }
  }

  /** What the repository stores for this text: trimmed, and null if blank. */
  private fun String.toNote(): String? = trim().take(MAX_NOTE_LENGTH).ifEmpty { null }
}
