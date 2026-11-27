package com.akrippner.mon

import com.akrippner.mon.core.data.di.dataModule
import com.akrippner.mon.core.database.di.databaseModule
import com.akrippner.mon.core.dictionary.di.dictionaryModule
import com.akrippner.mon.core.packs.di.packsModule
import com.akrippner.mon.core.tokenizer.di.tokenizerModule

internal actual val platformModules =
  listOf(databaseModule, dataModule, tokenizerModule, dictionaryModule, packsModule)
