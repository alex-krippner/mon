package com.akrippner.mon.feature.addreading.file

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberTextFilePicker(title: String, onPicked: (PickedFile) -> Unit): () -> Unit {
  val resolver = LocalContext.current.contentResolver
  val scope = rememberCoroutineScope()
  val currentOnPicked by rememberUpdatedState(onPicked)
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri == null) return@rememberLauncherForActivityResult
      // Asking the provider for the name and size may block (for example on a cloud drive), so
      // it is not done on the main thread.
      scope.launch {
        val file = withContext(Dispatchers.IO) { resolver.pickedFile(uri) }
        currentOnPicked(file)
      }
    }
  return remember(launcher) { { launcher.launch(PICKER_MIME_TYPES) } }
}

actual val supportsFileDrop: Boolean = false

@Composable
actual fun Modifier.textFileDropTarget(
  onHoverChange: (Boolean) -> Unit,
  onDrop: (PickedFile) -> Unit,
): Modifier = this

// Some providers report a .txt file as a generic binary file; the extension is checked afterwards.
private val PICKER_MIME_TYPES = arrayOf("text/plain", "application/octet-stream")

private fun ContentResolver.pickedFile(uri: Uri): PickedFile {
  var name: String? = null
  var size: Long? = null
  // A provider may refuse the query; the name then comes from the URI and the size stays unknown,
  // which the bounded read below still handles.
  try {
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
      ?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameColumn >= 0 && !cursor.isNull(nameColumn)) name = cursor.getString(nameColumn)
          val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) size = cursor.getLong(sizeColumn)
        }
      }
  } catch (_: RuntimeException) {}
  return PickedFile(name = name ?: uri.lastPathSegment.orEmpty(), size = size) {
    withContext(Dispatchers.IO) {
      val stream = openInputStream(uri) ?: throw IOException("Cannot open $uri")
      stream.use { it.readAtMost(MAX_FILE_BYTES + 1) }
    }
  }
}

/**
 * Reads at most [limit] bytes, so a file whose size is unknown cannot fill the memory. Reading one
 * byte more than the allowed size is enough for the decoder to refuse the file.
 */
private fun InputStream.readAtMost(limit: Long): ByteArray {
  val out = ByteArrayOutputStream()
  val buffer = ByteArray(BUFFER_SIZE)
  var remaining = limit
  while (remaining > 0) {
    val read = read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
    if (read < 0) break
    out.write(buffer, 0, read)
    remaining -= read
  }
  return out.toByteArray()
}

private const val BUFFER_SIZE = 8 * 1024
