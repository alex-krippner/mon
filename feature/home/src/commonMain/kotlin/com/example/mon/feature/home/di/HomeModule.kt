package com.example.mon.feature.home.di

import com.example.mon.feature.home.HomeViewModel
import org.koin.dsl.module

val homeModule = module {
    factory { HomeViewModel(get()) }
}