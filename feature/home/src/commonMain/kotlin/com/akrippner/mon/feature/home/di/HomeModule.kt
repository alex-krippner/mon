package com.akrippner.mon.feature.home.di

import com.akrippner.mon.feature.home.HomeViewModel
import org.koin.dsl.module

val homeModule = module {
    factory { HomeViewModel(get()) }
}