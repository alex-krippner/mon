package com.akrippner.mon.core.dictionary

/** A reader backed by a file that must be closed before the file is replaced or deleted. */
internal interface CloseableReader : PackReader {
  suspend fun close()
}
