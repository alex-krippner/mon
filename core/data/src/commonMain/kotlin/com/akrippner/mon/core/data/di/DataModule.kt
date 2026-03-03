package com.akrippner.mon.core.data.di

import com.akrippner.mon.core.data.SudachiDictionaryRepository
import com.akrippner.mon.core.data.model.DictionaryRepository
import org.koin.dsl.module

val dataModule = module {
    single<DictionaryRepository> { SudachiDictionaryRepository() }
}