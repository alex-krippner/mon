package com.example.mon

import com.example.mon.core.data.di.dataModule
import com.example.mon.core.domain.di.domainModule
import com.example.mon.feature.home.di.homeModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.KoinApplication

expect val platformModule: Module

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(domainModule, dataModule, homeModule, platformModule)
    }
}