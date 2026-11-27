package com.akrippner.mon.feature.addreading.file

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberTextFilePicker(title: String, onPicked: (PickedFile) -> Unit): () -> Unit {
  val currentOnPicked by rememberUpdatedState(onPicked)
  val currentTitle by rememberUpdatedState(title)
  return remember {
    {
      val dialog = FileDialog(null as Frame?, currentTitle, FileDialog.LOAD)
      dialog.setFilenameFilter { _, name -> name.endsWith(TXT_EXTENSION, ignoreCase = true) }
      // On Windows a pattern in the file name filters the list; on macOS it would show as a name.
      if (System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)) {
        dialog.file = "*$TXT_EXTENSION"
      }
      try {
        // Modal: returns when the reader has chosen or cancelled.
        dialog.isVisible = true
        val directory = dialog.directory
        val name = dialog.file
        if (directory != null && name != null) currentOnPicked(File(directory, name).toPickedFile())
      } finally {
        dialog.dispose()
      }
    }
  }
}

actual val supportsFileDrop: Boolean = true

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.textFileDropTarget(
  onHoverChange: (Boolean) -> Unit,
  onDrop: (PickedFile) -> Unit,
): Modifier {
  val currentOnHoverChange by rememberUpdatedState(onHoverChange)
  val currentOnDrop by rememberUpdatedState(onDrop)

  val target = remember {
    object : DragAndDropTarget {
      override fun onEntered(event: DragAndDropEvent) = currentOnHoverChange(true)

      override fun onExited(event: DragAndDropEvent) = currentOnHoverChange(false)

      override fun onEnded(event: DragAndDropEvent) = currentOnHoverChange(false)

      override fun onDrop(event: DragAndDropEvent): Boolean {
        currentOnHoverChange(false)
        // A malformed or non-local URI (for example a network path) cannot be read: not accepted.
        val file =
          runCatching {
              val uri = (event.dragData() as? DragData.FilesList)?.readFiles()?.firstOrNull()
              uri?.let { File(URI(it)) }
            }
            .getOrNull() ?: return false
        currentOnDrop(file.toPickedFile())
        return true
      }
    }
  }

  return dragAndDropTarget(
    shouldStartDragAndDrop = { event -> event.dragData() is DragData.FilesList },
    target = target,
  )
}

private const val TXT_EXTENSION = ".txt"

private fun File.toPickedFile(): PickedFile =
  PickedFile(name = name, size = length()) { withContext(Dispatchers.IO) { readBytes() } }
