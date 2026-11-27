package com.akrippner.mon.core.packs.di

import com.akrippner.mon.core.packs.CatalogError
import com.akrippner.mon.core.packs.Pack
import com.akrippner.mon.core.packs.PackManager
import com.akrippner.mon.core.packs.PackReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.dsl.module

// The browser has no pack files: the Sudachi and JMdict readers need a JVM and a file system. The
// web demo ships its own readings and look-ups instead, so nothing can be installed here.
actual val packsModule = module { single<PackManager> { UnavailablePackManager } }

/** A [PackManager] with nothing in it that can't fetch a catalog. */
internal object UnavailablePackManager : PackManager {
  override val packs: StateFlow<List<Pack>> = MutableStateFlow(emptyList())

  override suspend fun refreshCatalog(): CatalogError = CatalogError.NETWORK

  override suspend fun install(packId: String) = Unit

  override suspend fun remove(packId: String) = Unit

  override suspend fun readers(): List<PackReader> = emptyList()
}
