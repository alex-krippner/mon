package com.akrippner.mon.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a reading material entry in the database.
 *
 * A reading is a text in one language with a translation in another, both provided by the user.
 *
 * @property id Unique identifier for the reading, a UUID generated on the device.
 * @property userId The [UserEntity] that owns the reading.
 * @property title A descriptive title for the reading entry.
 * @property text The original text content.
 * @property translation The translated version of the text.
 * @property sourceLanguage BCP-47 language tag of [text], e.g. "ja".
 * @property translationLanguage BCP-47 language tag of [translation], e.g. "en".
 * @property createdAt Creation time in milliseconds since epoch.
 * @property updatedAt Last modification time in milliseconds since epoch.
 */
@Entity(
  tableName = "reading",
  foreignKeys =
    [
      ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("userId")],
)
data class ReadingEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val title: String,
  val text: String,
  val translation: String,
  val sourceLanguage: String,
  val translationLanguage: String,
  val createdAt: Long,
  val updatedAt: Long,
)
