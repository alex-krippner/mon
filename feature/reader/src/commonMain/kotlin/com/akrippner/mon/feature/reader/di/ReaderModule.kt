package com.akrippner.mon.feature.reader.di

import com.akrippner.mon.feature.reader.ReaderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {
  // Parameters: the reading id, and the id of a saved word to open at, or null.
  viewModel { parameters ->
    ReaderViewModel(
      readingId = parameters.values[0] as String,
      focusWordId = parameters.values.getOrNull(1) as String?,
      readings = get(),
      vocabulary = get(),
      lookupWord = get(),
      saveWord = get(),
      removeWord = get(),
      restoreWord = get(),
      setNote = get(),
    )
  }
}
