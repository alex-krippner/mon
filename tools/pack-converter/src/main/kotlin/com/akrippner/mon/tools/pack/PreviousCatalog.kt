package com.akrippner.mon.tools.pack

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private const val HTTP_OK = 200
private val TIMEOUT: Duration = Duration.ofSeconds(30)

/** The catalog the app fetches: the `catalog.json` of the latest release of [repo]. */
internal fun latestCatalogUrl(repo: String) =
  "https://github.com/$repo/releases/latest/download/catalog.json"

/**
 * Downloads the text at [url], following GitHub's redirect to the release asset.
 *
 * @throws IOException if the server can't be reached or answers with anything but 200. The HTTP
 *   client itself does not fail on a 404, and an error page must never be taken for a catalog.
 */
internal fun fetchText(url: String): String {
  val client =
    HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(TIMEOUT)
      .build()
  val request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build()
  val response =
    try {
      client.send(request, HttpResponse.BodyHandlers.ofString())
    } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
      throw IOException("Interrupted", e)
    }
  if (response.statusCode() != HTTP_OK) throw IOException("HTTP ${response.statusCode()}")
  return response.body()
}
