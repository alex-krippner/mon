package com.akrippner.mon.feature.reader

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.domain.model.LookupResult
import com.akrippner.mon.core.domain.usecase.LookupWordUseCase
import com.akrippner.mon.core.domain.usecase.RemoveSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.RestoreSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.SaveWordUseCase
import com.akrippner.mon.core.domain.usecase.SetSavedWordNoteUseCase
import com.akrippner.mon.core.domain.vocabulary.tokenIndexAtOffset
import com.akrippner.mon.core.domain.vocabulary.vocabularyForm
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.MAX_NOTE_LENGTH
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.ui.lookup.LookupUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val log = Logger.withTag("ReaderViewModel")

/**
 * Shows one reading, looks up the words the user taps, and saves them to the vocabulary.
 *
 * The reading is observed, so a reading that waited for its tokenizer switches from plain text to
 * tokens as soon as the analysis is saved. The reading's saved words are observed too: they are
 * drawn highlighted, counted in the header, and tell the look-up sheet whether the tapped word is
 * saved. Opening the reading is recorded by the library, which starts this screen, not here.
 */
class ReaderViewModel(
  private val readingId: String,
  /** A saved word to open the reading at, from "Show in reading"; null for a normal opening. */
  private val focusWordId: String?,
  private val readings: ReadingRepository,
  private val vocabulary: VocabularyRepository,
  private val lookupWord: LookupWordUseCase,
  private val saveWord: SaveWordUseCase,
  private val removeWord: RemoveSavedWordUseCase,
  private val restoreWord: RestoreSavedWordUseCase,
  private val setNote: SetSavedWordNoteUseCase,
) :
  BaseViewModel<ReaderState, ReaderIntent, ReaderEffect>(
    initialState = ReaderState(openedFromVocabulary = focusWordId != null)
  ) {

  private var reading: Reading? = null
  private var savedWords: List<SavedWord> = emptyList()
  private var savedForms: Set<String> = emptySet()

  /** The dictionary entries behind the open look-up sheet, to describe the word when saving it. */
  private var lookupEntries: List<DictionaryEntry> = emptyList()
  private var lookupJob: Job? = null

  /** The word to focus, once loaded; and whether the focus was worked out (or given up on). */
  private var focusWord: SavedWord? = null
  private var focusWordLoaded = false
  private var focusResolved = false

  init {
    if (focusWordId != null) loadFocusWord(focusWordId)

    readings
      .observeReading(readingId)
      // Room re-emits whenever the table changes, e.g. when another reading is tokenized or opened.
      // Only a real change of this reading is worth rebuilding the paragraphs.
      .distinctUntilChanged()
      .onEach(::showReading)
      .catch { e ->
        log.e(e) { "Could not observe reading $readingId" }
        updateState { copy(isLoading = false, error = ReaderError.LoadFailed) }
      }
      .launchIn(viewModelScope)

    vocabulary
      .observeForReading(readingId)
      .onEach(::showSavedWords)
      // The highlights and the count are extras: without them the reading is still readable.
      .catch { e -> log.e(e) { "Could not observe the saved words of reading $readingId" } }
      .launchIn(viewModelScope)
  }

  override fun onIntent(intent: ReaderIntent) {
    when (intent) {
      ReaderIntent.Back -> sendEffect(ReaderEffect.Close)
      ReaderIntent.ToggleFurigana -> updateState { copy(showFurigana = !showFurigana) }
      is ReaderIntent.TokenTapped -> {
        clearOutline()
        lookUp(intent.index)
      }
      ReaderIntent.RetryLookup -> state.value.selection?.let { lookUp(it.index) }
      ReaderIntent.DismissLookup -> {
        commitNote()
        dismissLookup()
      }
      ReaderIntent.OpenDictionaries -> sendEffect(ReaderEffect.OpenDictionaries)
      ReaderIntent.ToggleSave -> toggleSave()
      is ReaderIntent.NoteChanged ->
        updateState {
          val current = selection
          if (current?.saved != null) {
            copy(selection = current.copy(noteDraft = intent.note.capped()))
          } else {
            this
          }
        }
      ReaderIntent.NoteCommitted -> commitNote()
      is ReaderIntent.UndoRemove -> undoRemove(intent.word)
      ReaderIntent.OpenVocabulary -> sendEffect(ReaderEffect.OpenVocabulary(readingId))
      ReaderIntent.ClearOutline -> clearOutline()
    }
  }

  private fun showReading(reading: Reading?) {
    val tokensChanged = this.reading?.tokens != reading?.tokens
    this.reading = reading
    // Tokens only change from none to some, but a stale selection must never point at new tokens.
    if (tokensChanged) dismissLookup()

    if (reading == null) {
      updateState {
        ReaderState(isLoading = false, error = ReaderError.NotFound, showFurigana = showFurigana)
      }
      return
    }
    updateState {
      copy(
        title = reading.title,
        isLoading = false,
        error = null,
        isPending = !reading.isTokenized,
        tokenizerMayArrive = reading.sourceLanguage.hasTokenizerPack(),
        plainText = reading.text,
        paragraphs =
          if (reading.isTokenized) buildParagraphs(reading.tokens, savedForms) else emptyList(),
      )
    }
    resolveFocus()
  }

  private fun loadFocusWord(wordId: String) {
    viewModelScope.launch {
      try {
        focusWord = vocabulary.observeWord(wordId).first()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        log.w(e) { "Could not load the saved word $wordId to show" }
      }
      focusWordLoaded = true
      resolveFocus()
    }
  }

  /**
   * Works out which token the saved word to show is, once both the word and the tokens are there. A
   * word that is gone, or from another reading, leaves the reader open as usual.
   */
  private fun resolveFocus() {
    if (focusResolved || !focusWordLoaded) return
    val reading = reading?.takeIf { it.isTokenized } ?: return
    focusResolved = true
    val word = focusWord?.takeIf { it.readingId == readingId } ?: return
    val index = tokenIndexAtOffset(reading.tokens, word.sentence.wordOffset) ?: return
    updateState { copy(focus = ReaderFocus(tokenIndex = index)) }
  }

  private fun clearOutline() {
    updateState {
      if (focus?.outlined == true) copy(focus = focus.copy(outlined = false)) else this
    }
  }

  private fun showSavedWords(words: List<SavedWord>) {
    savedWords = words
    val forms = words.mapTo(HashSet()) { it.dictionaryForm }
    val formsChanged = forms != savedForms
    savedForms = forms

    updateState {
      val selection = selection?.withSavedWord(words)
      copy(
        savedCount = words.size,
        selection = selection,
        // Only a change of the forms moves highlights; a new note does not.
        paragraphs =
          if (formsChanged)
            reading?.takeIf { it.isTokenized }?.let { buildParagraphs(it.tokens, forms) }
              ?: paragraphs
          else paragraphs,
      )
    }
  }

  /**
   * The selection with the saved word of its token. The note draft follows the saved word only when
   * the word itself changes (saved, removed, restored), so typing is never overwritten.
   */
  private fun ReaderSelection.withSavedWord(words: List<SavedWord>): ReaderSelection {
    val form = reading?.tokens?.getOrNull(index)?.vocabularyForm()
    val word = words.firstOrNull { it.dictionaryForm == form }
    return if (word?.id == saved?.id) {
      copy(saved = word)
    } else {
      copy(saved = word, noteDraft = word?.note.orEmpty())
    }
  }

  private fun dismissLookup() {
    lookupJob?.cancel()
    lookupJob = null
    lookupEntries = emptyList()
    updateState { copy(selection = null) }
  }

  private fun lookUp(index: Int) {
    val reading = reading ?: return
    val token = reading.tokens.getOrNull(index) ?: return
    if (token.isPunctuation()) return

    // Tapping another word closes the sheet of the first one: keep what was typed.
    if (state.value.selection?.index != index) commitNote()

    val display = token.toDisplayToken()
    lookupJob?.cancel()
    lookupEntries = emptyList()
    updateState {
      copy(
        selection = ReaderSelection(index, display, LookupUiState.Loading).withSavedWord(savedWords)
      )
    }
    lookupJob =
      viewModelScope.launch {
        val outcome = search(token, reading, display.surface)
        lookupEntries = outcome.entries
        updateState {
          val current = selection
          if (current != null && current.index == index) {
            copy(
              selection =
                current.copy(lookup = outcome.state, canSave = outcome.entries.isNotEmpty())
            )
          } else {
            this
          }
        }
      }
  }

  private class LookupOutcome(
    val state: LookupUiState,
    val entries: List<DictionaryEntry> = emptyList(),
  )

  private suspend fun search(token: MonToken, reading: Reading, surface: String): LookupOutcome =
    try {
      when (
        val result = lookupWord.execute(token, reading.sourceLanguage, reading.translationLanguage)
      ) {
        is LookupResult.Found -> {
          // Keep only the entries that can be shown, so the first one on the sheet is the one
          // saved.
          val shown =
            result.entries.mapNotNull { entry -> entry.toJmdictEntry()?.let { entry to it } }
          if (shown.isEmpty()) {
            LookupOutcome(LookupUiState.NotFound(surface))
          } else {
            LookupOutcome(LookupUiState.Found(shown.map { it.second }), shown.map { it.first })
          }
        }
        LookupResult.NotFound -> LookupOutcome(LookupUiState.NotFound(surface))
        LookupResult.NoDictionaryInstalled -> LookupOutcome(LookupUiState.NoDictionaryInstalled)
        LookupResult.Unavailable -> LookupOutcome(LookupUiState.Unavailable)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      // The use case skips a failing dictionary itself, but not a failing provider. Whatever went
      // wrong, the sheet must not stay on "loading", and the user can retry.
      log.w(e) { "Lookup of '$surface' failed (${reading.sourceLanguage})" }
      LookupOutcome(LookupUiState.Unavailable)
    }

  private fun toggleSave() {
    val selection = state.value.selection ?: return
    val saved = selection.saved
    if (saved == null) {
      val entry = lookupEntries.firstOrNull() ?: return
      viewModelScope.launch {
        try {
          saveWord.execute(readingId, selection.index, entry)
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          log.w(e) { "Could not save '${selection.token.surface}'" }
          sendEffect(ReaderEffect.SaveFailed)
        }
      }
    } else {
      val draft = selection.noteDraft
      viewModelScope.launch {
        try {
          // Store what was typed first, so Undo brings the word back with its note.
          if (draft.toNote() != saved.note) setNote.execute(saved.id, draft)
          removeWord.execute(saved.id)?.let { sendEffect(ReaderEffect.ShowRemoved(it)) }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          log.w(e) { "Could not remove '${saved.surface}'" }
        }
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
    val selection = state.value.selection ?: return
    val saved = selection.saved ?: return
    val note = selection.noteDraft
    if (note.toNote() == saved.note) return
    viewModelScope.launch {
      try {
        setNote.execute(saved.id, note)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        log.w(e) { "Could not store the note of '${saved.surface}'" }
      }
    }
  }

  private fun String.capped(): String = take(MAX_NOTE_LENGTH)

  /** What the repository stores for this text: trimmed, and null if blank. */
  private fun String.toNote(): String? = trim().take(MAX_NOTE_LENGTH).ifEmpty { null }
}
