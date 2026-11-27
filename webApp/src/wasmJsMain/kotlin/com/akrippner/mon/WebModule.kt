package com.akrippner.mon

import com.akrippner.mon.core.data.InMemoryReadingRepository
import com.akrippner.mon.core.data.InMemoryVocabularyRepository
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.demo.DemoDictionaryProvider
import com.akrippner.mon.core.demo.DemoSeed
import com.akrippner.mon.core.dictionary.DictionaryProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * What the shared app leaves to the platform in the browser: where readings and saved words live
 * (in memory, gone on reload, starting with the readings of [seed]) and which dictionary answers a
 * look-up (the one made from [seed]).
 */
fun webModule(seed: DemoSeed): Module = module {
  single<ReadingRepository> { InMemoryReadingRepository(seed.toReadings()) }
  single<VocabularyRepository> { InMemoryVocabularyRepository(get()) }
  single<DictionaryProvider> { DemoDictionaryProvider(seed) }
}
