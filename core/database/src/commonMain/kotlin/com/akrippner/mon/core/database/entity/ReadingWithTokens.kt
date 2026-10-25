package com.akrippner.mon.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

/** A [ReadingEntity] together with its tokens. The order of [tokens] is not guaranteed. */
data class ReadingWithTokens(
  @Embedded val reading: ReadingEntity,
  @Relation(parentColumn = "id", entityColumn = "readingId") val tokens: List<ReadingTokenEntity>,
)
