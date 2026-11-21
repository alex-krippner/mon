package com.akrippner.mon.core.packs

/** A reader backed by a file that must be closed before the file is replaced or deleted. */
interface CloseableReader : PackReader {
  suspend fun close()
}
