package com.akrippner.mon.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.akrippner.mon.core.database.entity.UserEntity

@Dao
abstract class UserDao {
  @Query("SELECT * FROM user LIMIT 1") abstract suspend fun getLocalUser(): UserEntity?

  @Insert abstract suspend fun insert(user: UserEntity)

  /** Returns the existing local user, or stores and returns [candidate] if there is none. */
  @Transaction
  open suspend fun getOrCreateLocalUser(candidate: UserEntity): UserEntity {
    return getLocalUser() ?: candidate.also { insert(it) }
  }
}
