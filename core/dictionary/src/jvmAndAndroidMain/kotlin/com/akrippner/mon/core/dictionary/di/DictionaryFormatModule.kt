package com.akrippner.mon.core.dictionary.di

import com.akrippner.mon.core.dictionary.jmdict.JmdictFormat
import com.akrippner.mon.core.packs.PackFormat
import org.koin.dsl.bind
import org.koin.dsl.module

// bind, not single<PackFormat>: every module registers its own format under its own type, and the
// pack manager collects them with getAll<PackFormat>().
internal actual val dictionaryFormatModule = module {
  single { JmdictFormat } bind PackFormat::class
}
