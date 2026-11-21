package com.akrippner.mon.core.packs.di

import com.akrippner.mon.core.packs.PackFormat
import com.akrippner.mon.core.packs.PackManager
import com.akrippner.mon.core.packs.createPackManager
import java.io.File
import org.koin.dsl.module

// Next to the database (~/mon.db). The folder is still called "dictionaries": installed packs live
// there. single, not factory: the manager holds the state and readers.
actual val packsModule = module {
  single<PackManager> {
    createPackManager(
      File(System.getProperty("user.home"), ".mon/dictionaries"),
      getAll<PackFormat>(),
    )
  }
}
