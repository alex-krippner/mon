package com.akrippner.mon.feature.addreading

import androidx.compose.runtime.Immutable
import com.akrippner.mon.feature.addreading.file.PickedFile

@Immutable
data class AddReadingState(
  val title: String = "",
  val text: String = "",
  val isSaving: Boolean = false,
  /** The name of the file being read, or null when no file is being read. */
  val readingFileName: String? = null,
  val error: AddReadingError? = null,
  val showDiscardDialog: Boolean = false,
  /** True once the reading is stored; guards against importing twice while the screen closes. */
  val isSaved: Boolean = false,
) {
  val isReadingFile: Boolean
    get() = readingFileName != null

  /** The fields and buttons are locked while something is in progress. */
  val isLocked: Boolean
    get() = isSaving || isReadingFile || isSaved

  val canImport: Boolean
    get() = title.isNotBlank() && text.isNotBlank() && !isLocked

  /** Only a missing title is explained up front; an error takes the place of the hint. */
  val showTitleHint: Boolean
    get() = error == null && text.isNotBlank() && title.isBlank()
}

enum class AddReadingError {
  MissingTitle,
  MissingText,
  SaveFailed,
  FileNotText,
  FileTooLarge,
  FileEmpty,
  FileUnreadable,
}

sealed interface AddReadingIntent {
  data class TitleChanged(val value: String) : AddReadingIntent

  data class TextChanged(val value: String) : AddReadingIntent

  data object Import : AddReadingIntent

  /** The X or system back. Asks first when there is text that would be lost. */
  data object Close : AddReadingIntent

  data object DiscardConfirmed : AddReadingIntent

  data object DiscardDismissed : AddReadingIntent

  /** A file from Add file or a drop; read, checked and put into the text area. */
  data class FileChosen(val file: PickedFile) : AddReadingIntent
}

sealed interface AddReadingEffect {
  /** The reading was stored; the host should open it in the reader. */
  data class Imported(val readingId: String) : AddReadingEffect

  /** The screen should close without importing. */
  data object Closed : AddReadingEffect
}
