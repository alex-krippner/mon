package com.akrippner.mon.core.packs.di

import android.content.Context
import com.akrippner.mon.core.packs.PackFormat
import com.akrippner.mon.core.packs.PackManager
import com.akrippner.mon.core.packs.createPackManager
import java.io.File
import org.koin.dsl.module

// noBackupFilesDir keeps the large pack files out of the user's cloud backup. The folder is still
// called "dictionaries": installed packs live there. single, not factory: the manager holds the
// state and the open readers.
actual val packsModule = module {
  single<PackManager> {
    createPackManager(File(get<Context>().noBackupFilesDir, "dictionaries"), getAll<PackFormat>())
  }
}
