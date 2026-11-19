package com.akrippner.mon.core.domain.di

import com.akrippner.mon.core.domain.usecase.AddReadingUseCase
import com.akrippner.mon.core.domain.usecase.LookupWordUseCase
import com.akrippner.mon.core.domain.usecase.PendingTokenization
import com.akrippner.mon.core.domain.usecase.TokenizePendingReadingsUseCase
import org.koin.dsl.module

val domainModule = module {
  factory { AddReadingUseCase(get(), get()) }
  factory { LookupWordUseCase(get()) }
  factory { TokenizePendingReadingsUseCase(get(), get()) }
  single { PendingTokenization(get(), get()) }
}
