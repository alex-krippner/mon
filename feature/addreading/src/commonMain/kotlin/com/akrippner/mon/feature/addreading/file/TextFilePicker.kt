package com.akrippner.mon.feature.addreading.file

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Returns a function that opens the system picker for `.txt` files. [onPicked] gets the chosen
 * file, and is not called when the reader cancels. [title] is the dialog title where the platform
 * shows one.
 */
@Composable
expect fun rememberTextFilePicker(title: String, onPicked: (PickedFile) -> Unit): () -> Unit

/** True where a file can be dropped onto the text area. Only desktop supports it. */
expect val supportsFileDrop: Boolean

/**
 * Makes the element a drop target for files. [onHoverChange] reports a file being dragged over it;
 * [onDrop] gets the first dropped file. Does nothing where [supportsFileDrop] is false.
 */
@Composable
expect fun Modifier.textFileDropTarget(
  onHoverChange: (Boolean) -> Unit,
  onDrop: (PickedFile) -> Unit,
): Modifier
