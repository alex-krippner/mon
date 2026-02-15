package com.example.mon.ui.screens.home

import com.example.mon.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel : BaseViewModel<HomeState, HomeEffect>() {
    override val mutableState = MutableStateFlow(HomeState())
    override val effect = MutableSharedFlow<HomeEffect>()
}