package com.akrippner.mon.core.domain.di

import com.akrippner.mon.core.domain.usecase.TokenizeTextUseCase
import org.koin.dsl.module

val domainModule = module { single { TokenizeTextUseCase(get()) } }
