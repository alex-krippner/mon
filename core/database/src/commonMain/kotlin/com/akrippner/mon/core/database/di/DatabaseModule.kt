package com.akrippner.mon.core.database.di

import com.akrippner.mon.core.database.MonDatabase
import com.akrippner.mon.core.database.dao.ReadingDao
import com.akrippner.mon.core.database.dao.ReadingUsageDao
import com.akrippner.mon.core.database.dao.SavedWordDao
import com.akrippner.mon.core.database.dao.UserDao
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule: Module = module {
  includes(platformModule())
  single<UserDao> { get<MonDatabase>().getUserDao() }
  single<ReadingDao> { get<MonDatabase>().getReadingDao() }
  single<ReadingUsageDao> { get<MonDatabase>().getReadingUsageDao() }
  single<SavedWordDao> { get<MonDatabase>().getSavedWordDao() }
}
