package com.akrippner.mon.feature.vocabulary.di

import com.akrippner.mon.feature.vocabulary.VocabularyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vocabularyModule = module {
  // A reading id as the parameter makes the list of one reading; without one, this is the
  // Vocabulary tab, which lists all readings.
  viewModel { parameters ->
    VocabularyViewModel(
      readingId = parameters.getOrNull<String>(),
      vocabulary = get(),
      readings = get(),
      removeWord = get(),
      restoreWord = get(),
      setNote = get(),
    )
  }
}
