package com.example.mon.core.domain.di

import com.example.mon.core.domain.usecase.TokenizeTextUseCase
import org.koin.dsl.module

val domainModule = module {
    single { TokenizeTextUseCase(get()) }
}