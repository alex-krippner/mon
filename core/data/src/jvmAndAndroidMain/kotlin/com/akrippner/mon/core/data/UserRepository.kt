package com.akrippner.mon.core.data

import com.akrippner.mon.core.data.internal.newId
import com.akrippner.mon.core.data.internal.nowMillis
import com.akrippner.mon.core.database.dao.UserDao
import com.akrippner.mon.core.database.entity.UserEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Provides the user that owns the data on this device. */
class UserRepository(private val userDao: UserDao) {
  private val mutex = Mutex()
  private var cachedUserId: String? = null

  suspend fun currentUserId(): String =
    mutex.withLock {
      cachedUserId
        ?: userDao.getOrCreateLocalUser(UserEntity(id = newId(), createdAt = nowMillis())).id.also {
          cachedUserId = it
        }
    }
}
