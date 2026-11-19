package com.akrippner.mon.tools.pack

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val CATALOG = """{"packs":[]}"""

/** Runs against a server on this machine; nothing leaves it. */
class PreviousCatalogTest {
  private val server =
    HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0).apply {
      createContext("/asset/catalog.json") { it.respond(200, CATALOG) }
      // GitHub answers ".../releases/latest/download/catalog.json" with a redirect to the asset.
      createContext("/latest/catalog.json") {
        it.responseHeaders.add("Location", "/asset/catalog.json")
        it.respond(302, "")
      }
      createContext("/missing/catalog.json") { it.respond(404, "Not Found") }
      createContext("/broken/catalog.json") { it.respond(500, CATALOG) }
      start()
    }

  private val base = "http://${server.address.address.hostAddress}:${server.address.port}"

  private fun HttpExchange.respond(status: Int, body: String) {
    val bytes = body.toByteArray()
    sendResponseHeaders(status, if (bytes.isEmpty()) -1 else bytes.size.toLong())
    if (bytes.isNotEmpty()) responseBody.use { it.write(bytes) }
    close()
  }

  @AfterTest
  fun stop() {
    server.stop(0)
  }

  @Test
  fun downloadsTheCatalog() {
    assertEquals(CATALOG, fetchText("$base/asset/catalog.json"))
  }

  @Test
  fun followsTheRedirectOfTheLatestRelease() {
    assertEquals(CATALOG, fetchText("$base/latest/catalog.json"))
  }

  @Test
  fun anErrorPageIsAFailureNotACatalog() {
    // The HTTP client itself returns a 404 like any other answer. A repository without a release,
    // or a release without a catalog.json, must stop the run instead of becoming "no packs".
    val notFound = assertFailsWith<IOException> { fetchText("$base/missing/catalog.json") }
    assertTrue(notFound.message.orEmpty().contains("404"), notFound.message)
    // Even if the body happens to look like a catalog.
    assertFailsWith<IOException> { fetchText("$base/broken/catalog.json") }
  }

  @Test
  fun anUnreachableServerIsAFailure() {
    server.stop(0)

    assertFailsWith<IOException> { fetchText("$base/asset/catalog.json") }
  }

  @Test
  fun latestCatalogUrlPointsAtTheLatestReleasesCatalog() {
    // This only pins the URL's shape. The catalog that is merged must be the one the app sees,
    // i.e. CATALOG_URL in :core:packs (PackManagers.kt), but that constant is internal
    // to the other module, so this test cannot notice if the two drift apart. Keep them equal by
    // hand when either changes.
    assertEquals(
      "https://github.com/alex-krippner/mon-data/releases/latest/download/catalog.json",
      latestCatalogUrl("alex-krippner/mon-data"),
    )
  }
}
