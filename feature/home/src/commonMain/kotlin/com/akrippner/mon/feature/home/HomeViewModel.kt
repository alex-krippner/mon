package com.akrippner.mon.feature.home

import com.akrippner.mon.core.domain.usecase.TokenizeTextUseCase
import com.akrippner.mon.feature.home.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val tokenizeTextUseCase: TokenizeTextUseCase) : BaseViewModel<HomeState, HomeEffect>() {
    override val mutableState = MutableStateFlow(HomeState())
    override val effect = MutableSharedFlow<HomeEffect>()

    init {
        tokenizeSampleText()
    }

    private fun tokenizeSampleText() {
        viewModelScope.launch {
            val sampleText = "すもももももももものうち"
            val tokens = tokenizeTextUseCase.execute(sampleText)
            mutableState.value = mutableState.value.copy(tokens = tokens)
        }
    }
}