package com.example.mon

import com.example.mon.data.SudachiDictionaryRepository
import com.example.mon.domain.DictionaryRepository
import com.example.mon.domain.usecase.TokenizeTextUseCase
import com.example.mon.ui.screens.home.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.core.KoinApplication

expect val platformModule: Module

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }
}

val commonModule = module {
    single<DictionaryRepository> { SudachiDictionaryRepository() }
    single { TokenizeTextUseCase(get()) }
    factory { HomeViewModel() }
}
