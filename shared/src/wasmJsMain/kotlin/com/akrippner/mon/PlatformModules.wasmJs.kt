package com.akrippner.mon

import com.akrippner.mon.core.packs.di.packsModule
import com.akrippner.mon.core.tokenizer.di.tokenizerModule

// No database and no dictionary pack: the web app supplies ReadingRepository and
// DictionaryProvider.
// The tokenizer provider finds no tokenizer, so new readings stay pending.
internal actual val platformModules = listOf(tokenizerModule, packsModule)
