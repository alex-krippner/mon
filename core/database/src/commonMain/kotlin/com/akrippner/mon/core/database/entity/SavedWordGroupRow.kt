package com.akrippner.mon.core.database.entity

import androidx.room.Embedded

/**
 * A saved word with the title of its reading and the time the reading was last opened, for the
 * vocabulary list that groups words by reading.
 */
data class SavedWordGroupRow(
  @Embedded val word: SavedWordEntity,
  val readingTitle: String,
  val lastOpenedAt: Long,
)
