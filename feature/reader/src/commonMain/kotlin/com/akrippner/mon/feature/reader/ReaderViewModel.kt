package com.akrippner.mon.feature.reader

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.domain.model.LookupResult
import com.akrippner.mon.core.domain.usecase.LookupWordUseCase
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.ui.lookup.LookupUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val log = Logger.withTag("ReaderViewModel")

/**
 * Shows one reading and looks up the words the user taps.
 *
 * The reading is observed, so a reading that waited for its tokenizer switches from plain text to
 * tokens as soon as the analysis is saved. Opening the reading is recorded by the library, which
 * starts this screen, not here.
 */
class ReaderViewModel(
  private val readingId: String,
  private val readings: ReadingRepository,
  private val lookupWord: LookupWordUseCase,
) : BaseViewModel<ReaderState, ReaderIntent, ReaderEffect>(initialState = ReaderState()) {

  private var reading: Reading? = null
  private var lookupJob: Job? = null

  init {
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
  }

  override fun onIntent(intent: ReaderIntent) {
    when (intent) {
      ReaderIntent.Back -> sendEffect(ReaderEffect.Close)
      ReaderIntent.ToggleFurigana -> updateState { copy(showFurigana = !showFurigana) }
      is ReaderIntent.TokenTapped -> lookUp(intent.index)
      ReaderIntent.RetryLookup -> state.value.selection?.let { lookUp(it.index) }
      ReaderIntent.DismissLookup -> dismissLookup()
      ReaderIntent.OpenDictionaries -> sendEffect(ReaderEffect.OpenDictionaries)
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
        paragraphs = if (reading.isTokenized) buildParagraphs(reading.tokens) else emptyList(),
      )
    }
  }

  private fun dismissLookup() {
    lookupJob?.cancel()
    lookupJob = null
    updateState { copy(selection = null) }
  }

  private fun lookUp(index: Int) {
    val reading = reading ?: return
    val token = reading.tokens.getOrNull(index) ?: return
    if (token.isPunctuation()) return

    val display = token.toDisplayToken()
    lookupJob?.cancel()
    updateState { copy(selection = ReaderSelection(index, display, LookupUiState.Loading)) }
    lookupJob =
      viewModelScope.launch {
        val result = search(token, reading, display.surface)
        updateState {
          val current = selection
          if (current != null && current.index == index) {
            copy(selection = current.copy(lookup = result))
          } else {
            this
          }
        }
      }
  }

  private suspend fun search(token: MonToken, reading: Reading, surface: String): LookupUiState =
    try {
      when (
        val result = lookupWord.execute(token, reading.sourceLanguage, reading.translationLanguage)
      ) {
        is LookupResult.Found -> {
          val entries = result.entries.mapNotNull { it.toJmdictEntry() }
          if (entries.isEmpty()) LookupUiState.NotFound(surface) else LookupUiState.Found(entries)
        }
        LookupResult.NotFound -> LookupUiState.NotFound(surface)
        LookupResult.NoDictionaryInstalled -> LookupUiState.NoDictionaryInstalled
        LookupResult.Unavailable -> LookupUiState.Unavailable
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      // The use case skips a failing dictionary itself, but not a failing provider. Whatever went
      // wrong, the sheet must not stay on "loading", and the user can retry.
      log.w(e) { "Lookup of '$surface' failed (${reading.sourceLanguage})" }
      LookupUiState.Unavailable
    }
}
