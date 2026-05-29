package com.akrippner.mon.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.akrippner.mon.core.database.entity.ReadingEntity

@Dao
interface ReadingDao {
  @Insert suspend fun insert(reading: ReadingEntity): Long

  @Query("SELECT * FROM ReadingEntity") suspend fun getAll(): List<ReadingEntity>

  @Update suspend fun update(reading: ReadingEntity): Int

  @Delete suspend fun delete(reading: ReadingEntity): Int
}
