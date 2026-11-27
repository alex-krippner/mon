package com.akrippner.mon.core.data

import com.akrippner.mon.core.data.internal.newId
import com.akrippner.mon.core.data.internal.nowMillis
import com.akrippner.mon.core.data.internal.toStoredNote
import com.akrippner.mon.core.data.mapper.toDomain
import com.akrippner.mon.core.data.mapper.toEntity
import com.akrippner.mon.core.database.dao.SavedWordDao
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class RoomVocabularyRepository(
  private val savedWordDao: SavedWordDao,
  private val userRepository: UserRepository,
) : VocabularyRepository {

  override fun observeForReading(readingId: String): Flow<List<SavedWord>> =
    savedWordDao.observeForReading(readingId).map { words -> words.map { it.toDomain() } }

  override fun observeGroups(): Flow<List<VocabularyGroup>> = flow {
    val userId = userRepository.currentUserId()
    emitAll(
      savedWordDao.observeGroupRows(userId).map { rows ->
        // The rows arrive sorted by reading, so a group is a run of rows with the same reading.
        // A LinkedHashMap keeps the readings in that order.
        val groups = LinkedHashMap<String, Pair<String, MutableList<SavedWord>>>()
        for (row in rows) {
          groups.getOrPut(row.word.readingId) { row.readingTitle to mutableListOf() }.second +=
            row.word.toDomain()
        }
        groups.map { (readingId, group) -> VocabularyGroup(readingId, group.first, group.second) }
      }
    )
  }

  override fun observeCount(readingId: String): Flow<Int> = savedWordDao.observeCount(readingId)

  override fun observeSaved(readingId: String, dictionaryForm: String): Flow<SavedWord?> =
    savedWordDao.observeByForm(readingId, dictionaryForm).map { it?.toDomain() }

  override fun observeWord(id: String): Flow<SavedWord?> =
    savedWordDao.observeById(id).map { it?.toDomain() }

  override suspend fun save(word: NewSavedWord): SavedWord =
    savedWordDao.saveIfAbsent(word.toEntity(id = newId(), savedAt = nowMillis())).toDomain()

  override suspend fun remove(id: String): SavedWord? = savedWordDao.remove(id)?.toDomain()

  override suspend fun restore(word: SavedWord) {
    savedWordDao.insertOrReplace(word.toEntity())
  }

  override suspend fun setNote(id: String, note: String?) {
    savedWordDao.setNote(id, note.toStoredNote())
  }
}
