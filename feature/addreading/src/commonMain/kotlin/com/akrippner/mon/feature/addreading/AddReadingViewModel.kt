package com.akrippner.mon.feature.addreading

import androidx.lifecycle.viewModelScope
import com.akrippner.mon.core.arch.mvi.BaseViewModel
import com.akrippner.mon.core.domain.model.AddReadingInput
import com.akrippner.mon.core.domain.model.AddReadingInputError
import com.akrippner.mon.core.domain.model.AddReadingResult
import com.akrippner.mon.core.domain.usecase.AddReadingUseCase
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.feature.addreading.file.PickedFile
import com.akrippner.mon.feature.addreading.file.TextFileResult
import com.akrippner.mon.feature.addreading.file.checkBeforeReading
import com.akrippner.mon.feature.addreading.file.decodeTextFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AddReadingViewModel(private val addReadingUseCase: AddReadingUseCase) :
  BaseViewModel<AddReadingState, AddReadingIntent, AddReadingEffect>(
    initialState = AddReadingState()
  ) {

  override fun onIntent(intent: AddReadingIntent) {
    when (intent) {
      is AddReadingIntent.TitleChanged ->
        if (!state.value.isLocked) updateState { copy(title = intent.value, error = null) }
      is AddReadingIntent.TextChanged ->
        if (!state.value.isLocked) updateState { copy(text = intent.value, error = null) }
      AddReadingIntent.Import -> import()
      AddReadingIntent.Close -> close()
      AddReadingIntent.DiscardConfirmed -> {
        updateState { copy(showDiscardDialog = false) }
        sendEffect(AddReadingEffect.Closed)
      }
      AddReadingIntent.DiscardDismissed -> updateState { copy(showDiscardDialog = false) }
      is AddReadingIntent.FileChosen -> readFile(intent.file)
    }
  }

  private fun close() {
    val current = state.value
    // Not while something is in progress: a file read would replace the text under the dialog.
    if (current.isLocked) return
    // Typed or pasted text would be lost, so ask first. An empty screen closes at once.
    if (current.text.isNotBlank()) {
      updateState { copy(showDiscardDialog = true) }
    } else {
      sendEffect(AddReadingEffect.Closed)
    }
  }

  private fun readFile(file: PickedFile) {
    if (state.value.isLocked) return

    val rejected = checkBeforeReading(file.name, file.size)
    if (rejected != null) {
      updateState { copy(error = rejected) }
      return
    }
    updateState { copy(readingFileName = file.name, error = null) }

    viewModelScope.launch {
      val bytes =
        try {
          file.readBytes()
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          updateState { copy(readingFileName = null, error = AddReadingError.FileUnreadable) }
          return@launch
        }

      when (val result = decodeTextFile(file.name, bytes)) {
        // A file is a whole text, so it replaces what is there. The file name only fills an empty
        // title, so a typed title is never overwritten.
        is TextFileResult.Text ->
          updateState {
            copy(
              text = result.text,
              title = title.ifBlank { result.title },
              readingFileName = null,
              error = null,
            )
          }
        is TextFileResult.Rejected ->
          updateState { copy(readingFileName = null, error = result.error) }
      }
    }
  }

  private fun import() {
    val current = state.value
    if (current.isSaving || current.isSaved || current.isReadingFile) return
    updateState { copy(isSaving = true, error = null) }

    viewModelScope.launch {
      val input =
        AddReadingInput(
          title = current.title,
          text = current.text,
          translation = NO_TRANSLATION,
          sourceLanguage = SOURCE_LANGUAGE,
          translationLanguage = TRANSLATION_LANGUAGE,
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

      when (result) {
        is AddReadingResult.Success -> {
          updateState { copy(isSaving = false, isSaved = true) }
          sendEffect(AddReadingEffect.Imported(result.readingId))
        }
        is AddReadingResult.Invalid ->
          updateState { copy(isSaving = false, error = result.errors.first().toError()) }
      }
    }
  }

  private fun AddReadingInputError.toError(): AddReadingError =
    when (this) {
      AddReadingInputError.MissingTitle -> AddReadingError.MissingTitle
      AddReadingInputError.MissingText -> AddReadingError.MissingText
    }

  private companion object {
    // Fixed until the screen has a language control: only a Japanese tokenizer exists, and the
    // translation language is the gloss language for look-ups. The screen has no translation field.
    val SOURCE_LANGUAGE = Language.JAPANESE
    val TRANSLATION_LANGUAGE = Language.ENGLISH
    const val NO_TRANSLATION = ""
  }
}
