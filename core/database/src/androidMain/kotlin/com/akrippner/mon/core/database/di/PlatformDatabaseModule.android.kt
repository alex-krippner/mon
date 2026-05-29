package com.akrippner.mon.core.database.di

import com.akrippner.mon.core.database.MonDatabase
import com.akrippner.mon.core.database.getDatabaseBuilder
import com.akrippner.mon.core.database.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
  single<MonDatabase> {
    val builder = getDatabaseBuilder(get())
    getRoomDatabase(builder)
  }
}
