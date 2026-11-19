package com.akrippner.mon.core.tokenizer.di

import android.content.Context
import com.akrippner.mon.core.tokenizer.BundledTokenizerProvider
import com.akrippner.mon.core.tokenizer.TokenizerImpl
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import com.akrippner.mon.core.tokenizer.createSudachiDictionary
import org.koin.dsl.module

actual val tokenizerModule = module {
  single<TokenizerProvider> {
    val context = get<Context>()
    BundledTokenizerProvider(TokenizerImpl({ createSudachiDictionary(context) }))
  }
}
