package com.akrippner.mon.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<MonDatabase> {
  val dbFile = File(System.getProperty("user.home"), "mon.db")
  return Room.databaseBuilder<MonDatabase>(name = dbFile.absolutePath)
}
