package com.akrippner.mon.core.database.di

import com.akrippner.mon.core.database.MonDatabase
import com.akrippner.mon.core.database.dao.ReadingDao
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule: Module = module {
  includes(platformModule())
  single<ReadingDao> { get<MonDatabase>().getReadingDao() }
}
