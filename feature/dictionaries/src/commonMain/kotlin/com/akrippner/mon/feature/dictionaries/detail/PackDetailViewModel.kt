package com.akrippner.mon.feature.dictionaries.detail

import androidx.lifecycle.viewModelScope
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.dictionary.pack.DictionaryManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** One pack's details. Follows the pack in the manager's list, so progress shows here as well. */
class PackDetailViewModel(private val packId: String, private val manager: DictionaryManager) :
  BaseViewModel<PackDetailState, PackDetailIntent, PackDetailEffect>(PackDetailState()) {

  /**
   * A removal was confirmed and closes the screen itself once done. Until the pack is listed again,
   * its vanishing from the list (an installed pack the catalog no longer has) must not close the
   * screen a second time, which would also pop the list below it.
   */
  private var removeRequested = false

  init {
    manager.packs
      .onEach { packs ->
        val pack = packs.firstOrNull { it.info.id == packId }
        if (pack != null) {
          removeRequested = false
        } else if (state.value.pack != null && !removeRequested) {
          // The pack was shown and has left the catalog: there is nothing left to show.
          sendEffect(PackDetailEffect.Close)
        }
        updateState { copy(pack = pack) }
      }
      .launchIn(viewModelScope)
  }

  override fun onIntent(intent: PackDetailIntent) {
    when (intent) {
      PackDetailIntent.Download -> viewModelScope.launch { manager.install(packId) }
      PackDetailIntent.AskRemove -> updateState { copy(confirmingRemove = true) }
      PackDetailIntent.CancelRemove -> updateState { copy(confirmingRemove = false) }
      PackDetailIntent.ConfirmRemove -> {
        if (!state.value.confirmingRemove) return // a second tap on the dialog's Remove
        removeRequested = true
        updateState { copy(confirmingRemove = false) }
        viewModelScope.launch {
          manager.remove(packId)
          sendEffect(PackDetailEffect.Close)
        }
      }
      PackDetailIntent.Back -> sendEffect(PackDetailEffect.Close)
    }
  }
}
