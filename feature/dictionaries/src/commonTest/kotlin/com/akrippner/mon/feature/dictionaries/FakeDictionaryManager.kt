package com.akrippner.mon.feature.dictionaries

import com.akrippner.mon.core.dictionary.PackReader
import com.akrippner.mon.core.dictionary.pack.CatalogError
import com.akrippner.mon.core.dictionary.pack.CatalogPack
import com.akrippner.mon.core.dictionary.pack.DictionaryManager
import com.akrippner.mon.core.dictionary.pack.Pack
import com.akrippner.mon.core.dictionary.pack.PackState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDictionaryManager : DictionaryManager {
  override val packs = MutableStateFlow<List<Pack>>(emptyList())

  /** When set, refreshCatalog() waits until it is completed. */
  var refreshGate: CompletableDeferred<Unit>? = null
  var refreshCount = 0
  /** What refreshCatalog() returns; null means it succeeded. */
  var refreshResult: CatalogError? = null
  val installs = mutableListOf<String>()
  val removals = mutableListOf<String>()

  override suspend fun refreshCatalog(): CatalogError? {
    refreshCount++
    refreshGate?.await()
    return refreshResult
  }

  override suspend fun install(packId: String) {
    installs += packId
  }

  /** Runs at the end of remove(), e.g. to change [packs] as the real manager would. */
  var onRemove: () -> Unit = {}

  override suspend fun remove(packId: String) {
    removals += packId
    onRemove()
  }

  override suspend fun readers(): List<PackReader> = emptyList()
}

fun testPack(id: String = "jmdict-eng", state: PackState = PackState.NotInstalled) =
  Pack(
    info =
      CatalogPack(
        id = id,
        name = "JMdict ($id)",
        sourceLanguage = "ja",
        glossLanguage = "en",
        format = "jmdict-sqlite",
        schema = 1,
        version = "1",
        url = "https://example.test/$id.db.gz",
        sizeBytes = 1,
        unpackedSizeBytes = 1,
        sha256 = "x",
        attribution = "attribution",
      ),
    state = state,
  )
