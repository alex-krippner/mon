package com.akrippner.mon.feature.home.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
//TODO: move to core
interface UiState
interface UiEffect


abstract class BaseViewModel<S: UiState, E: UiEffect> {
    protected abstract val mutableState: MutableStateFlow<S>
    val state: StateFlow<S> get() = mutableState

    abstract val effect: Flow<E>

    protected val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}