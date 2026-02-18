package com.example.mon.core.data.di

import com.example.mon.core.data.SudachiDictionaryRepository
import com.example.mon.core.data.model.DictionaryRepository
import org.koin.dsl.module

val dataModule = module {
    single<DictionaryRepository> { SudachiDictionaryRepository() }
}