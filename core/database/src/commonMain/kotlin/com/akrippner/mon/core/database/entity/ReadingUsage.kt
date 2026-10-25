package com.akrippner.mon.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a historical record of when a specific [ReadingEntity] was accessed or used.
 *
 * This entity is used to track user engagement and progress over time.
 *
 * @property id Unique identifier for this usage record, a UUID generated on the device.
 * @property userId The [UserEntity] that used the reading.
 * @property readingId The ID of the [ReadingEntity] associated with this usage record.
 * @property usedAt The time of the usage event in milliseconds since epoch.
 */
@Entity(
  tableName = "reading_usage",
  foreignKeys =
    [
      ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = ReadingEntity::class,
        parentColumns = ["id"],
        childColumns = ["readingId"],
        onDelete = ForeignKey.CASCADE,
      ),
    ],
  indices = [Index("userId"), Index("readingId")],
)
data class ReadingUsageEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val readingId: String,
  val usedAt: Long,
)
