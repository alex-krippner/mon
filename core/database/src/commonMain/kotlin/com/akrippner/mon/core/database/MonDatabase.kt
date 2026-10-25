package com.akrippner.mon.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akrippner.mon.core.database.converter.StringListConverter
import com.akrippner.mon.core.database.dao.ReadingDao
import com.akrippner.mon.core.database.dao.ReadingUsageDao
import com.akrippner.mon.core.database.dao.UserDao
import com.akrippner.mon.core.database.entity.ReadingEntity
import com.akrippner.mon.core.database.entity.ReadingTokenEntity
import com.akrippner.mon.core.database.entity.ReadingUsageEntity
import com.akrippner.mon.core.database.entity.UserEntity
import kotlinx.coroutines.Dispatchers

@Database(
  entities =
    [UserEntity::class, ReadingEntity::class, ReadingTokenEntity::class, ReadingUsageEntity::class],
  version = 2,
)
@TypeConverters(StringListConverter::class)
@ConstructedBy(MonDatabaseConstructor::class)
abstract class MonDatabase : RoomDatabase() {
  abstract fun getUserDao(): UserDao

  abstract fun getReadingDao(): ReadingDao

  abstract fun getReadingUsageDao(): ReadingUsageDao
}

@Suppress("KotlinNoActualForExpect")
expect object MonDatabaseConstructor : RoomDatabaseConstructor<MonDatabase> {
  override fun initialize(): MonDatabase
}

fun getRoomDatabase(builder: RoomDatabase.Builder<MonDatabase>): MonDatabase {
  return builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    // TODO: replace with real migrations before the first release.
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()
}
