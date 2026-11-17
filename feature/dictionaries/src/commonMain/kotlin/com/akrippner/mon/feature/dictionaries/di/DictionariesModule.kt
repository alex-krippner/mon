package com.akrippner.mon.feature.dictionaries.di

import com.akrippner.mon.feature.dictionaries.DictionariesViewModel
import com.akrippner.mon.feature.dictionaries.detail.PackDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dictionariesModule = module {
  viewModelOf(::DictionariesViewModel)
  viewModel { (packId: String) -> PackDetailViewModel(packId, get()) }
}
