package com.akrippner.mon.feature.library

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.model.data.ReadingSummary

@Immutable
data class LibraryState(
  val readings: List<ReadingSummary> = emptyList(),
  val isLoading: Boolean = true,
  val error: LibraryError? = null,
)

enum class LibraryError {
  LoadFailed
}

sealed interface LibraryIntent {
  data class OpenReading(val readingId: String) : LibraryIntent

  data object AddReading : LibraryIntent
}

sealed interface LibraryEffect {
  data class OpenReader(val readingId: String) : LibraryEffect

  data object OpenAddReading : LibraryEffect
}
