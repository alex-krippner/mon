package com.akrippner.mon.core.tokenizer.di

import android.content.Context
import com.akrippner.mon.core.tokenizer.Tokenizer
import com.akrippner.mon.core.tokenizer.TokenizerImpl
import com.akrippner.mon.core.tokenizer.createSudachiDictionary
import org.koin.dsl.module

actual val tokenizerModule = module {
  single<Tokenizer> {
    val context = get<Context>()
    TokenizerImpl { createSudachiDictionary(context) }
  }
}
