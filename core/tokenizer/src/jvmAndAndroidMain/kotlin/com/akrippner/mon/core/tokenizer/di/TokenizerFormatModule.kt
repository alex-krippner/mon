package com.akrippner.mon.core.tokenizer.di

import com.akrippner.mon.core.packs.PackFormat
import com.akrippner.mon.core.tokenizer.sudachi.SudachiFormat
import org.koin.dsl.bind
import org.koin.dsl.module

// bind, not single<PackFormat>: every module registers its own format under its own type, and the
// pack manager collects them with getAll<PackFormat>().
internal actual val tokenizerFormatModule = module {
  single { SudachiFormat } bind PackFormat::class
}
