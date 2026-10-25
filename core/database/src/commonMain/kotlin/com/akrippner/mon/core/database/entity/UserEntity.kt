package com.akrippner.mon.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The person who owns the data stored in the app.
 *
 * There is a single local user until sign-in is added. The id is a UUID generated on the device, so
 * it stays unique once data is synced between devices.
 *
 * @property id Unique identifier of the user.
 * @property createdAt Creation time in milliseconds since epoch.
 */
@Entity(tableName = "user") data class UserEntity(@PrimaryKey val id: String, val createdAt: Long)
