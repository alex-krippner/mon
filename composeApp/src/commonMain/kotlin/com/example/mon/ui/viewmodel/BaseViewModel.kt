package com.example.mon.ui.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface UiState
interface UiEffect


abstract class BaseViewModel<S: UiState, E: UiEffect> {
    protected abstract val mutableState: MutableStateFlow<S>
    val state: StateFlow<S> get() = mutableState

    abstract val effect: Flow<E>
}