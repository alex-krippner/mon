package com.akrippner.mon.feature.library

import androidx.lifecycle.viewModelScope
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.data.ReadingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: ReadingRepository) :
  BaseViewModel<LibraryState, LibraryIntent, LibraryEffect>(initialState = LibraryState()) {

  init {
    repository
      .observeReadingSummaries()
      .catch { updateState { copy(isLoading = false, error = LibraryError.LoadFailed) } }
      .onEach { readings ->
        updateState { copy(readings = readings, isLoading = false, error = null) }
      }
      .launchIn(viewModelScope)
  }

  override fun onIntent(intent: LibraryIntent) {
    when (intent) {
      is LibraryIntent.OpenReading -> {
        sendEffect(LibraryEffect.OpenReader(intent.readingId))
        recordOpened(intent.readingId)
      }
      LibraryIntent.AddReading -> sendEffect(LibraryEffect.OpenAddReading)
    }
  }

  private fun recordOpened(readingId: String) {
    viewModelScope.launch {
      try {
        repository.recordReadingOpened(readingId)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        // Usage tracking is best effort: e.g. the reading may have been deleted meanwhile.
      }
    }
  }
}
