package com.akrippner.mon

import com.akrippner.mon.core.data.di.dataModule
import com.akrippner.mon.core.database.di.databaseModule
import com.akrippner.mon.core.domain.di.domainModule
import com.akrippner.mon.core.tokenizer.di.tokenizerModule
import com.akrippner.mon.feature.home.di.homeModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
  startKoin {
    appDeclaration()
    modules(databaseModule, domainModule, dataModule, homeModule, tokenizerModule)
  }
}
