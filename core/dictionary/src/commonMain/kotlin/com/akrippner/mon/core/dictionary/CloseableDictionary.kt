package com.akrippner.mon.core.dictionary

/** A dictionary backed by a file that must be closed before the file is replaced or deleted. */
internal interface CloseableDictionary : MonDictionary {
  suspend fun close()
}
