package com.akrippner.mon.core.ui.lookup

import com.akrippner.mon.core.ui.JmdictEntry

sealed interface LookupUiState {
  data object Loading : LookupUiState

  data class NotFound(val surface: String) : LookupUiState

  data class Error(val onRetry: () -> Unit) : LookupUiState

  data class Found(val entry: JmdictEntry, val savedToVocab: Boolean = false) : LookupUiState
}
