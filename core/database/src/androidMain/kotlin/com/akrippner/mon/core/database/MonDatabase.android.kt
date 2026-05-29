package com.akrippner.mon.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<MonDatabase> {
  val appContext = context.applicationContext
  val dbFile = appContext.getDatabasePath("mon.db")
  return Room.databaseBuilder<MonDatabase>(context = appContext, name = dbFile.absolutePath)
}
