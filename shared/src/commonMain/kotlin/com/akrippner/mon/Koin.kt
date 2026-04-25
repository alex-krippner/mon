package com.akrippner.mon

import com.akrippner.mon.core.data.di.dataModule
import com.akrippner.mon.core.domain.di.domainModule
import com.akrippner.mon.feature.home.di.homeModule
import org.koin.core.context.startKoin
import org.koin.core.KoinApplication


fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(domainModule, dataModule, homeModule)
    }
}