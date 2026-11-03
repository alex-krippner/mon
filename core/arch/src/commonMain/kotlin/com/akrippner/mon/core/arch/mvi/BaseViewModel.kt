package com.akrippner.mon.core.arch.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Base class for MVI-lite view models.
 *
 * The screen talks to the view model in three types:
 * - [Intent] is input from the UI: something the user did or asked for (a tap, a text change). It
 *   enters through [onIntent], the only way to change the view model from outside, so the sealed
 *   intent type lists everything the screen can do.
 * - [State] is what the screen renders and is replayed to new collectors.
 * - [Effect] is a one-off event (navigation, a snackbar) that must be handled exactly once, so it
 *   is delivered through a channel instead of the state. Use [NoEffect] when a screen has none.
 */
abstract class BaseViewModel<State, Intent, Effect>(initialState: State) : ViewModel() {

  private val _state = MutableStateFlow(initialState)
  val state: StateFlow<State> = _state.asStateFlow()

  private val _effects = Channel<Effect>(Channel.BUFFERED)

  /** Effects are buffered until a collector is present and each one is delivered once. */
  val effects: Flow<Effect> = _effects.receiveAsFlow()

  abstract fun onIntent(intent: Intent)

  protected fun updateState(reduce: State.() -> State) {
    _state.update { it.reduce() }
  }

  protected fun sendEffect(effect: Effect) {
    _effects.trySend(effect)
  }
}

/** The [BaseViewModel] effect type for screens that emit no effects. */
sealed interface NoEffect
