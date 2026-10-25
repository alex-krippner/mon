package com.akrippner.mon.feature.addreading.di

import com.akrippner.mon.feature.addreading.AddReadingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val addReadingModule = module { viewModelOf(::AddReadingViewModel) }
