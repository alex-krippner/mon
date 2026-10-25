package com.akrippner.mon.feature.library

import androidx.lifecycle.viewModelScope
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.data.ReadingRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: ReadingRepository) :
  BaseViewModel<LibraryState, LibraryIntent>(initialState = LibraryState()) {

  init {
    repository
      .observeReadings()
      .onEach { readings -> updateState { copy(readings = readings, isLoading = false) } }
      .launchIn(viewModelScope)
  }

  override fun onIntent(intent: LibraryIntent) {
    when (intent) {
      is LibraryIntent.OpenReading ->
        viewModelScope.launch { repository.recordReadingOpened(intent.readingId) }
    }
  }
}
