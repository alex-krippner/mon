package com.akrippner.mon.feature.dictionaries

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.packs.CatalogError
import com.akrippner.mon.core.packs.Pack

@Immutable
data class DictionariesState(
  /** The catalog joined with what is installed; installed packs show even when offline. */
  val packs: List<Pack> = emptyList(),
  /**
   * The packs with a version on disk, shown in their own section. A pack that is downloading keeps
   * the section it was in, so an update does not jump to "Available" and back.
   */
  val installedIds: Set<String> = emptySet(),
  val isRefreshing: Boolean = true,
  /** Why the last refresh failed; the packs shown may then be out of date. Null after a success. */
  val refreshError: CatalogError? = null,
  /** The name of a pack whose download finished while the screen was open, for a "ready" note. */
  val readyPackName: String? = null,
) {
  val installed: List<Pack>
    get() = packs.filter { it.info.id in installedIds }

  val available: List<Pack>
    get() = packs.filterNot { it.info.id in installedIds }
}

sealed interface DictionariesIntent {
  /** Also used to update an installed pack and to retry a failed download. */
  data class Download(val packId: String) : DictionariesIntent

  /** Show a pack's details and licence. */
  data class OpenPack(val packId: String) : DictionariesIntent

  /** Go back to reading, from the "ready" note. */
  data object OpenLibrary : DictionariesIntent

  data object DismissReady : DictionariesIntent

  /** Fetch the catalog again. */
  data object Refresh : DictionariesIntent
}

sealed interface DictionariesEffect {
  data class OpenPack(val packId: String) : DictionariesEffect

  data object OpenLibrary : DictionariesEffect
}
