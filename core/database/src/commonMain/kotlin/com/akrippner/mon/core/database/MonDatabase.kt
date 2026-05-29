package com.akrippner.mon.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akrippner.mon.core.database.dao.ReadingDao
import com.akrippner.mon.core.database.entity.ReadingEntity
import kotlinx.coroutines.Dispatchers

@Database(entities = [ReadingEntity::class], version = 1)
@ConstructedBy(MonDatabaseConstructor::class)
abstract class MonDatabase : RoomDatabase() {
  abstract fun getReadingDao(): ReadingDao
}

@Suppress("KotlinNoActualForExpect")
expect object MonDatabaseConstructor : RoomDatabaseConstructor<MonDatabase> {
  override fun initialize(): MonDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<MonDatabase>): MonDatabase {
  return builder.setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
}
