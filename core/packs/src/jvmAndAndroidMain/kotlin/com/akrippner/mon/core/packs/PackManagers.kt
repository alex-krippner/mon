package com.akrippner.mon.core.packs

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** The catalog is the only URL the app hard-codes; it points to the pack files. */
internal const val CATALOG_URL =
  "https://github.com/alex-krippner/mon-data/releases/latest/download/catalog.json"

/**
 * Builds the manager for both platforms; each platform only chooses [baseDir]. [formats] are the
 * file formats the app can read, registered by the modules that own them.
 */
internal fun createPackManager(baseDir: File, formats: List<PackFormat>): PackManager {
  val client =
    HttpClient(OkHttp) {
      install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000 // time without data; no overall limit, files are big
      }
    }
  val downloader = KtorPackDownloader(client)
  return FilePackManager(
    downloader = downloader,
    installer = PackInstaller(downloader, baseDir),
    factory = PackFormats(formats),
    catalogUrl = CATALOG_URL,
    // The manager is an app-wide singleton, so its scope is never cancelled.
    scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
  )
}
