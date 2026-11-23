package com.akrippner.mon.feature.dictionaries

import androidx.lifecycle.viewModelScope
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackManager
import com.akrippner.mon.core.packs.PackState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Shows the packs and starts downloads. Progress and failures arrive through the manager's `packs`;
 * downloads run in the manager's own scope, so leaving the screen does not cancel them.
 */
class DictionariesViewModel(private val manager: PackManager) :
  BaseViewModel<DictionariesState, DictionariesIntent, DictionariesEffect>(
    initialState = DictionariesState()
  ) {

  private var refreshJob: Job? = null

  init {
    manager.packs.onEach(::onPacks).launchIn(viewModelScope)
    refresh()
  }

  override fun onIntent(intent: DictionariesIntent) {
    when (intent) {
      is DictionariesIntent.Download -> {
        updateState { copy(readyPackName = null) }
        viewModelScope.launch { manager.install(intent.packId) }
      }
      is DictionariesIntent.OpenPack -> sendEffect(DictionariesEffect.OpenPack(intent.packId))
      DictionariesIntent.OpenLibrary -> {
        updateState { copy(readyPackName = null) }
        sendEffect(DictionariesEffect.OpenLibrary)
      }
      DictionariesIntent.DismissReady -> updateState { copy(readyPackName = null) }
      DictionariesIntent.Refresh -> {
        updateState { copy(readyPackName = null) }
        refresh()
      }
    }
  }

  private fun onPacks(packs: List<Pack>) {
    updateState {
      val before = this.packs.associate { it.info.id to it.state }
      val finished =
        packs.lastOrNull {
          it.state == PackState.Installed && before[it.info.id] is PackState.Downloading
        }
      copy(
        packs = packs,
        installedIds = packs.filter { it.onDisk != null }.map { it.info.id }.toSet(),
        readyPackName = finished?.info?.name ?: readyPackName,
      )
    }
  }

  private fun refresh() {
    if (refreshJob?.isActive == true) return
    refreshJob =
      viewModelScope.launch {
        updateState { copy(isRefreshing = true, refreshError = null) }
        try {
          val error = manager.refreshCatalog()
          updateState { copy(refreshError = error) }
        } finally {
          updateState { copy(isRefreshing = false) }
        }
      }
  }
}
