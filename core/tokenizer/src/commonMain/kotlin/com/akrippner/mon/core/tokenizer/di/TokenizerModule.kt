package com.akrippner.mon.core.tokenizer.di

import com.akrippner.mon.core.tokenizer.PackTokenizerProvider
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/** The tokenizer file formats and the provider that reads the installed packs. */
val tokenizerModule = module {
  includes(tokenizerFormatModule)
  single<TokenizerProvider> { PackTokenizerProvider(get()) }
}

/** Registers the tokenizer formats with the pack manager. */
internal expect val tokenizerFormatModule: Module
