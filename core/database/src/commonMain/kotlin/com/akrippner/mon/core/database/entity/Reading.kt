package com.akrippner.mon.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReadingEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val text: String,
  val translation: String,
  val title: String,
)
