package com.akrippner.mon.core.packs

import java.io.File

// TODO: Move to commonMain if iOS support needed
interface PackDownloader {
  /** GET [url] as text, e.g. the catalog. Throws PackException(NETWORK). */
  suspend fun fetchText(url: String): String

  /** Streams [url] into [target], reporting 0f..1f. Throws PackException(NETWORK or STORAGE). */
  suspend fun download(url: String, target: File, fallbackBytes: Long, onProgress: (Float) -> Unit)
}
