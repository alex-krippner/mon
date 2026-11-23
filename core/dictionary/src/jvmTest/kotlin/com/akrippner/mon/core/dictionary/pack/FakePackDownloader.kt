package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.packs.*
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred

internal class FakePackDownloader : PackDownloader {
  val texts = mutableMapOf<String, String>() // url -> catalog JSON
  val files = mutableMapOf<String, ByteArray>() // url -> .db.gz bytes
  var failWith: PackError? = null // every call throws PackException(it)
  var gate: CompletableDeferred<Unit>? = null // download() waits here until you complete it
  val downloads = AtomicInteger()
  /** The last progress download() reports; a server whose size was off never reaches 1. */
  var finalProgress = 1f

  override suspend fun fetchText(url: String): String {
    failWith?.let { throw PackException(it) }
    return texts[url] ?: throw PackException(PackError.NETWORK)
  }

  override suspend fun download(
    url: String,
    target: File,
    fallbackBytes: Long,
    onProgress: (Float) -> Unit,
  ) {
    downloads.incrementAndGet()
    failWith?.let { throw PackException(it) }
    gate?.await()
    target.writeBytes(files[url] ?: throw PackException(PackError.NETWORK))
    onProgress(finalProgress)
  }
}
