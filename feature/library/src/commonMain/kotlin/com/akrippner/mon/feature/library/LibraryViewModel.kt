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
  BaseViewModel<LibraryState, LibraryIntent>(initialState = LibraryState()) {

  init {
    repository
      .observeReadings()
      .catch { error ->
        updateState {
          copy(isLoading = false, errorMessage = error.message ?: "Could not load readings.")
        }
      }
      .onEach { readings ->
        updateState { copy(readings = readings, isLoading = false, errorMessage = null) }
      }
      .launchIn(viewModelScope)
  }

  override fun onIntent(intent: LibraryIntent) {
    when (intent) {
      is LibraryIntent.OpenReading ->
        viewModelScope.launch {
          try {
            repository.recordReadingOpened(intent.readingId)
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            // Usage tracking is best effort: e.g. the reading may have been deleted meanwhile.
          }
        }
    }
  }
}
