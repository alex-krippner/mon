package com.akrippner.mon.core.arch.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State, Intent>(initialState: State) : ViewModel() {

  private val _state = MutableStateFlow(initialState)
  val state: StateFlow<State> = _state.asStateFlow()

  abstract fun onIntent(intent: Intent)

  protected fun updateState(reduce: State.() -> State) {
    _state.update { it.reduce() }
  }
}
