package com.akrippner.mon.core.dictionary.pack

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import java.io.File
import java.io.IOException

private const val BUFFER_SIZE = 64 * 1024

internal class KtorPackDownloader(private val client: HttpClient) : PackDownloader {
  override suspend fun fetchText(url: String): String =
    try {
      val response = client.get(url)
      if (!response.status.isSuccess()) throw PackException(PackError.NETWORK)
      response.bodyAsText()
    } catch (e: IOException) {
      throw PackException(PackError.NETWORK)
    }

  override suspend fun download(
    url: String,
    target: File,
    fallbackBytes: Long,
    onProgress: (Float) -> Unit,
  ) {
    try {
      client.prepareGet(url).execute { response ->
        if (!response.status.isSuccess()) throw PackException(PackError.NETWORK)
        val channel = response.bodyAsChannel()
        val total = (response.contentLength() ?: fallbackBytes).coerceAtLeast(1)
        val buffer = ByteArray(BUFFER_SIZE)
        var received = 0L
        storage { target.outputStream() }
          .use { out ->
            while (true) {
              val read = channel.readAvailable(buffer)
              if (read == -1) break
              storage { out.write(buffer, 0, read) }
              received += read
              onProgress((received.toFloat() / total).coerceAtMost(1f))
            }
          }
      }
    } catch (
      e: IOException) { // file errors are already PackException(STORAGE), which passes through
      throw PackException(PackError.NETWORK)
    }
  }
}
