package com.akrippner.mon

import org.koin.core.module.Module

/**
 * The Koin modules that depend on the platform's storage and file support. The JVM and Android use
 * Room and the downloadable packs; the browser has neither, so its app provides the readings and
 * the dictionary itself through `initKoin { modules(...) }`.
 */
internal expect val platformModules: List<Module>
