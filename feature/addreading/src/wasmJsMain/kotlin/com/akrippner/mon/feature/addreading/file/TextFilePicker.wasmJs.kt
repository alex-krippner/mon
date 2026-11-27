package com.akrippner.mon.feature.addreading.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import kotlin.js.Promise
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File

@Composable
actual fun rememberTextFilePicker(title: String, onPicked: (PickedFile) -> Unit): () -> Unit {
  val currentOnPicked by rememberUpdatedState(onPicked)
  return remember {
    {
      // A new input for every click, so choosing the same file twice still reports a change.
      val input = document.createElement("input") as HTMLInputElement
      input.type = "file"
      input.accept = ".txt,text/plain"
      input.onchange = {
        input.files?.item(0)?.let { file ->
          currentOnPicked(
            PickedFile(name = file.name, size = file.size.toDouble().toLong()) {
              file.readAllBytes()
            }
          )
        }
        null
      }
      input.click()
    }
  }
}

actual val supportsFileDrop: Boolean = false

@Composable
actual fun Modifier.textFileDropTarget(
  onHoverChange: (Boolean) -> Unit,
  onDrop: (PickedFile) -> Unit,
): Modifier = this

// The decoder refuses a file by its size before it reads the bytes, so this reads the whole file.
private suspend fun File.readAllBytes(): ByteArray {
  val bytes = Int8Array(arrayBuffer(this).await())
  return ByteArray(bytes.length) { bytes[it] }
}

private fun arrayBuffer(file: File): Promise<ArrayBuffer> = js("file.arrayBuffer()")
