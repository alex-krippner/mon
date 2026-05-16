package com.akrippner.mon.core.data.di

import com.akrippner.mon.core.data.JapaneseDictionaryRepository
import org.koin.dsl.module

val dataModule = module { single { JapaneseDictionaryRepository() } }
