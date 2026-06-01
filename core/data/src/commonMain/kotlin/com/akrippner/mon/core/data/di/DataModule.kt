package com.akrippner.mon.core.data.di

import com.akrippner.mon.core.data.JapaneseRepository
import org.koin.dsl.module

val dataModule = module { single { JapaneseRepository(get()) } }
