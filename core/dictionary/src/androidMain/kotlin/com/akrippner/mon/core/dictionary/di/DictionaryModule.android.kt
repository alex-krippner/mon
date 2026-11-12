package com.akrippner.mon.core.dictionary.di

import android.content.Context
import com.akrippner.mon.core.dictionary.createDictionaryManager
import com.akrippner.mon.core.dictionary.pack.DictionaryManager
import java.io.File
import org.koin.dsl.module

// noBackupFilesDir keeps the large dictionary files out of the user's cloud backup.
// single, not factory: the manager holds the state and the open readers.
actual val dictionaryModule = module {
  single<DictionaryManager> {
    createDictionaryManager(File(get<Context>().noBackupFilesDir, "dictionaries"))
  }
}
