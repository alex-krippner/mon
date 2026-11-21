package com.akrippner.mon.core.dictionary.di

import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.PackDictionaryProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/** The dictionary file formats and the provider that reads the installed packs. */
val dictionaryModule = module {
  includes(dictionaryFormatModule)
  single<DictionaryProvider> { PackDictionaryProvider(get()) }
}

/** Registers the dictionary formats with the pack manager. */
internal expect val dictionaryFormatModule: Module
