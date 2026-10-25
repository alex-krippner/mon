package com.akrippner.mon.core.data.di

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.data.RoomReadingRepository
import com.akrippner.mon.core.data.UserRepository
import org.koin.dsl.module

val dataModule = module {
  single { UserRepository(get()) }
  single<ReadingRepository> { RoomReadingRepository(get(), get(), get()) }
}
