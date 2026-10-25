package com.akrippner.mon.feature.home

import androidx.lifecycle.viewModelScope
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.domain.usecase.TokenizeTextUseCase
import kotlinx.coroutines.launch

class HomeViewModel(private val tokenizeTextUseCase: TokenizeTextUseCase) :
  BaseViewModel<HomeState, HomeIntent>(initialState = HomeState()) {

  init {
    tokenizeSampleText()
  }

  override fun onIntent(intent: HomeIntent) = Unit

  private fun tokenizeSampleText() {
    viewModelScope.launch {
      val sampleText = "新幹線"
      val tokens = tokenizeTextUseCase.execute(sampleText)
      updateState { copy(tokens = tokens) }
    }
  }
}
