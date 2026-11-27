package com.akrippner.mon.core.domain.di

import com.akrippner.mon.core.domain.usecase.AddReadingUseCase
import com.akrippner.mon.core.domain.usecase.LookupWordUseCase
import com.akrippner.mon.core.domain.usecase.PendingTokenization
import com.akrippner.mon.core.domain.usecase.RemoveSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.RestoreSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.SaveWordUseCase
import com.akrippner.mon.core.domain.usecase.SetSavedWordNoteUseCase
import com.akrippner.mon.core.domain.usecase.TokenizePendingReadingsUseCase
import org.koin.dsl.module

val domainModule = module {
  factory { AddReadingUseCase(get(), get(), get<PendingTokenization>()::request) }
  factory { LookupWordUseCase(get()) }
  factory { TokenizePendingReadingsUseCase(get(), get()) }
  single { PendingTokenization(get(), get()) }
  factory { SaveWordUseCase(get(), get()) }
  factory { RemoveSavedWordUseCase(get()) }
  factory { RestoreSavedWordUseCase(get()) }
  factory { SetSavedWordNoteUseCase(get()) }
}
