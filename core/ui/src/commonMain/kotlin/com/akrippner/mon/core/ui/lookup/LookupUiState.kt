package com.akrippner.mon.core.ui.lookup

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.ui.JmdictEntry

/** What the look-up sheet shows. Holds no callbacks: the screen passes those to the sheet. */
sealed interface LookupUiState {
  data object Loading : LookupUiState

  /** A dictionary is installed, but it has nothing for the word. */
  data class NotFound(val surface: String) : LookupUiState

  /** No installed dictionary translates this language pair; the sheet offers to get one. */
  data object NoDictionaryInstalled : LookupUiState

  /** A dictionary could not answer right now. Asking again may work. */
  data object Unavailable : LookupUiState

  data class Found(val entries: List<JmdictEntry>) : LookupUiState
}

/** The texts of the look-up sheet. `:core:ui` has no resources, so the feature supplies them. */
@Immutable
data class LookupStrings(
  val notFound: String,
  val unavailable: String,
  val noDictionary: String,
  val openDictionaries: String,
  val retry: String,
)
