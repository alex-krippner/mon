package com.akrippner.mon.feature.addreading

import androidx.lifecycle.viewModelScope
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.domain.model.AddReadingInput
import com.akrippner.mon.core.domain.model.AddReadingInputError
import com.akrippner.mon.core.domain.model.AddReadingResult
import com.akrippner.mon.core.domain.usecase.AddReadingUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AddReadingViewModel(private val addReadingUseCase: AddReadingUseCase) :
  BaseViewModel<AddReadingState, AddReadingIntent>(initialState = AddReadingState()) {

  override fun onIntent(intent: AddReadingIntent) {
    when (intent) {
      is AddReadingIntent.TitleChanged -> updateState { copy(title = intent.value, error = null) }
      is AddReadingIntent.TextChanged -> updateState { copy(text = intent.value, error = null) }
      is AddReadingIntent.TranslationChanged ->
        updateState { copy(translation = intent.value, error = null) }
      AddReadingIntent.Save -> save()
    }
  }

  private fun save() {
    val current = state.value
    if (current.isSaving || current.savedReadingId != null) return
    updateState { copy(isSaving = true, error = null) }

    viewModelScope.launch {
      val input =
        AddReadingInput(
          title = current.title,
          text = current.text,
          translation = current.translation,
          sourceLanguage = current.sourceLanguage,
          translationLanguage = current.translationLanguage,
        )
      val result =
        try {
          addReadingUseCase.execute(input)
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          updateState { copy(isSaving = false, error = AddReadingError.SaveFailed) }
          return@launch
        }

      updateState {
        when (result) {
          is AddReadingResult.Success -> copy(isSaving = false, savedReadingId = result.readingId)
          is AddReadingResult.Invalid ->
            copy(isSaving = false, error = result.errors.first().toError())
        }
      }
    }
  }

  private fun AddReadingInputError.toError(): AddReadingError =
    when (this) {
      AddReadingInputError.MissingTitle -> AddReadingError.MissingTitle
      AddReadingInputError.MissingText -> AddReadingError.MissingText
    }
}
