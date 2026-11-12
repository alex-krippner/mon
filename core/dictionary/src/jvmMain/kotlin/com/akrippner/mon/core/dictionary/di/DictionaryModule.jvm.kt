package com.akrippner.mon.core.dictionary.di

import com.akrippner.mon.core.dictionary.createDictionaryManager
import com.akrippner.mon.core.dictionary.pack.DictionaryManager
import java.io.File
import org.koin.dsl.module

// Next to the database (~/mon.db). single, not factory: the manager holds the state and readers.
actual val dictionaryModule = module {
  single<DictionaryManager> {
    createDictionaryManager(File(System.getProperty("user.home"), ".mon/dictionaries"))
  }
}
