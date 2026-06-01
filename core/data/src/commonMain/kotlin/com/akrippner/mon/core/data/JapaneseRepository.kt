package com.akrippner.mon.core.data

import com.akrippner.mon.core.database.dao.ReadingDao
import com.akrippner.mon.core.database.entity.ReadingEntity

class JapaneseRepository(private val readingDao: ReadingDao) {

  suspend fun getReadings(): List<ReadingEntity> {
    return readingDao.getAll()
  }

  suspend fun addReading(reading: ReadingEntity) {
    readingDao.insert(reading)
  }

  suspend fun updateReading(reading: ReadingEntity) {
    readingDao.update(reading)
  }

  suspend fun deleteReading(reading: ReadingEntity) {
    readingDao.delete(reading)
  }
}
