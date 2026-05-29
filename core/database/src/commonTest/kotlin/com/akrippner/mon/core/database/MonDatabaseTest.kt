package com.akrippner.mon.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akrippner.mon.core.database.entity.ReadingEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class MonDatabaseTest {
  private lateinit var db: MonDatabase

  @BeforeTest
  fun setup() {
    // 1. Create the in-memory builder
    val builder: RoomDatabase.Builder<MonDatabase> =
      Room.inMemoryDatabaseBuilder<MonDatabase>(factory = MonDatabaseConstructor::initialize)

    // 2. Configure the driver and build
    // We use BundledSQLiteDriver so the behavior is identical on Android and JVM
    db = builder.setDriver(BundledSQLiteDriver()).build()
  }

  @AfterTest
  fun tearDown() {
    db.close()
  }

  @Test
  fun insertAndReadTest() = runTest {
    val dao = db.getReadingDao()

    // Replace with your actual Entity and DAO methods
    val entity = ReadingEntity(japanese = "こんにちは", translation = "Hello", title = "Hello World")
    dao.insert(entity)
    val result = dao.getAll()
    assertEquals(1, result.size)
  }

  @Test
  fun updateTest() = runTest {
    val dao = db.getReadingDao()
    val entity = ReadingEntity(japanese = "こんにちは", translation = "Hello", title = "Hello World")
    val id = dao.insert(entity)
    val updatedEntity = entity.copy(translation = "Hallo", id = id)
    dao.update(updatedEntity)
    val result = dao.getAll()
    assertEquals("Hallo", result[0].translation)
  }

  @Test
  fun deleteTest() = runTest {
    val dao = db.getReadingDao()
    val entity = ReadingEntity(japanese = "こんにちは", translation = "Hello", title = "Hello World")
    val id = dao.insert(entity)
    dao.delete(entity.copy(id = id))
    val result = dao.getAll()
    assertEquals(0, result.size)
  }
}
