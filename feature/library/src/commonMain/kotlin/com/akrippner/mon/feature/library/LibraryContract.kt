package com.akrippner.mon.feature.library

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.model.data.ReadingSummary

@Immutable
data class LibraryState(
  val readings: List<ReadingSummary> = emptyList(),
  val isLoading: Boolean = true,
  val errorMessage: String? = null,
)

sealed interface LibraryIntent {
  data class OpenReading(val readingId: String) : LibraryIntent
}
