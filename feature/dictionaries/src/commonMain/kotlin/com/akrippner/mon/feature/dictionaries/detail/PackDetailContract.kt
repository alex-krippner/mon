package com.akrippner.mon.feature.dictionaries.detail

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.dictionary.pack.Pack

@Immutable
data class PackDetailState(
  /** Null until the manager lists the pack, e.g. right after the app was restored. */
  val pack: Pack? = null,
  /** The "Remove …?" dialog is open. */
  val confirmingRemove: Boolean = false,
)

sealed interface PackDetailIntent {
  /** Download, update or retry, as on the tile. */
  data object Download : PackDetailIntent

  /** Ask before removing: getting the pack back means downloading it again. */
  data object AskRemove : PackDetailIntent

  data object ConfirmRemove : PackDetailIntent

  data object CancelRemove : PackDetailIntent

  data object Back : PackDetailIntent
}

sealed interface PackDetailEffect {
  /** Leave the screen: on back, after a removal, or when the pack is gone from the list. */
  data object Close : PackDetailEffect
}
