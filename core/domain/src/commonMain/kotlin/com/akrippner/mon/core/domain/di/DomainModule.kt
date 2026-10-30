package com.akrippner.mon.core.domain.di

import com.akrippner.mon.core.domain.usecase.AddReadingUseCase
import org.koin.dsl.module

val domainModule = module { factory { AddReadingUseCase(get(), get()) } }
