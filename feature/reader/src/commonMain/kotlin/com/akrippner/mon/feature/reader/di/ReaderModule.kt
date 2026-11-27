package com.akrippner.mon.feature.reader.di

import com.akrippner.mon.feature.reader.ReaderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {
  viewModel { (readingId: String) -> ReaderViewModel(readingId, get(), get()) }
}
