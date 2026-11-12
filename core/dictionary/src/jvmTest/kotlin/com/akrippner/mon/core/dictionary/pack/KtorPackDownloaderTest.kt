package com.akrippner.mon.core.dictionary.pack

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class KtorPackDownloaderTest {
  private val dir = Files.createTempDirectory("ktor-test").toFile()

  @AfterTest
  fun cleanUp() {
    dir.deleteRecursively()
  }

  private fun downloader(handler: MockRequestHandler) =
    KtorPackDownloader(HttpClient(MockEngine(handler)))

  @Test
  fun fetchesText() = runTest {
    val downloader = downloader { respond("hello") }

    assertEquals("hello", downloader.fetchText("https://example.test/catalog.json"))
  }

  @Test
  fun textFetchWithHttpErrorIsANetworkError() = runTest {
    val downloader = downloader { respondError(HttpStatusCode.NotFound) }

    val error = assertFailsWith<PackException> { downloader.fetchText("https://example.test/x") }

    assertEquals(PackError.NETWORK, error.error)
  }

  @Test
  fun textFetchWithConnectionFailureIsANetworkError() = runTest {
    val downloader = downloader { throw IOException("offline") }

    val error = assertFailsWith<PackException> { downloader.fetchText("https://example.test/x") }

    assertEquals(PackError.NETWORK, error.error)
  }

  @Test
  fun downloadWritesTheFileAndReportsProgress() = runTest {
    val bytes = ByteArray(200_000) { (it % 251).toByte() } // several 64 KB chunks
    val downloader = downloader {
      respond(bytes, headers = headersOf(HttpHeaders.ContentLength, bytes.size.toString()))
    }
    val target = File(dir, "pack.db.gz")
    val progress = mutableListOf<Float>()

    downloader.download("https://example.test/pack", target, fallbackBytes = 1) { progress += it }

    assertContentEquals(bytes, target.readBytes())
    assertTrue(progress.isNotEmpty())
    assertEquals(progress.sorted(), progress, "progress must never go backwards")
    assertEquals(1f, progress.last())
  }

  @Test
  fun downloadWithHttpErrorIsANetworkError() = runTest {
    val downloader = downloader { respondError(HttpStatusCode.InternalServerError) }

    val error =
      assertFailsWith<PackException> {
        downloader.download("https://example.test/pack", File(dir, "a.gz"), 1) {}
      }

    assertEquals(PackError.NETWORK, error.error)
  }

  @Test
  fun downloadWithConnectionFailureIsANetworkError() = runTest {
    val downloader = downloader { throw IOException("offline") }

    val error =
      assertFailsWith<PackException> {
        downloader.download("https://example.test/pack", File(dir, "a.gz"), 1) {}
      }

    assertEquals(PackError.NETWORK, error.error)
  }

  @Test
  fun unwritableTargetIsAStorageErrorNotANetworkError() = runTest {
    val downloader = downloader { respond(ByteArray(10)) }
    val target = File(dir, "missing-folder/a.gz")

    val error =
      assertFailsWith<PackException> {
        downloader.download("https://example.test/pack", target, 1) {}
      }

    assertEquals(PackError.STORAGE, error.error)
  }
}
