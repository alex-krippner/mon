package com.akrippner.mon

import com.akrippner.mon.core.data.di.dataModule
import com.akrippner.mon.core.database.di.databaseModule
import com.akrippner.mon.core.dictionary.di.dictionaryModule
import com.akrippner.mon.core.domain.di.domainModule
import com.akrippner.mon.core.domain.usecase.PendingTokenization
import com.akrippner.mon.core.packs.di.packsModule
import com.akrippner.mon.core.tokenizer.di.tokenizerModule
import com.akrippner.mon.feature.addreading.di.addReadingModule
import com.akrippner.mon.feature.dictionaries.di.dictionariesModule
import com.akrippner.mon.feature.library.di.libraryModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
  val koin =
    startKoin {
        appDeclaration()
        modules(
          databaseModule,
          domainModule,
          dataModule,
          libraryModule,
          addReadingModule,
          dictionariesModule,
          tokenizerModule,
          dictionaryModule,
          packsModule,
        )
      }
      .koin

  // Readings stored without tokens get them once a tokenizer is available. The app-wide scope is
  // never cancelled, like the pack manager's.
  koin.get<PendingTokenization>().start(CoroutineScope(SupervisorJob() + Dispatchers.Default))
}
