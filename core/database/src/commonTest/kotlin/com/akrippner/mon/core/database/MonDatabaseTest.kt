package com.akrippner.mon.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akrippner.mon.core.database.entity.ReadingEntity
import com.akrippner.mon.core.database.entity.ReadingTokenEntity
import com.akrippner.mon.core.database.entity.ReadingUsageEntity
import com.akrippner.mon.core.database.entity.UserEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class MonDatabaseTest {
  private lateinit var db: MonDatabase

  @BeforeTest
  fun setup() {
    val builder: RoomDatabase.Builder<MonDatabase> =
      Room.inMemoryDatabaseBuilder<MonDatabase>(factory = MonDatabaseConstructor::initialize)

    // We use BundledSQLiteDriver so the behavior is identical on Android and JVM
    db = builder.setDriver(BundledSQLiteDriver()).build()
  }

  @AfterTest
  fun tearDown() {
    db.close()
  }

  private suspend fun createUser(id: String = "user-1"): UserEntity {
    return db.getUserDao().getOrCreateLocalUser(UserEntity(id = id, createdAt = 0))
  }

  private fun reading(id: String, userId: String = "user-1", createdAt: Long = 0) =
    ReadingEntity(
      id = id,
      userId = userId,
      title = "Title $id",
      text = "こんにちは",
      translation = "Hello",
      sourceLanguage = "ja",
      translationLanguage = "en",
      createdAt = createdAt,
      updatedAt = createdAt,
    )

  private fun token(readingId: String, index: Int, surface: String = "新幹線") =
    ReadingTokenEntity(
      readingId = readingId,
      tokenIndex = index,
      surface = surface,
      pronunciation = "しんかんせん",
      dictionaryForm = surface,
      partOfSpeech = listOf("名詞", "普通名詞"),
    )

  @Test
  fun getOrCreateLocalUser_isIdempotent() = runTest {
    val first = createUser("user-1")
    val second = createUser("user-2")

    assertEquals("user-1", first.id)
    assertEquals(first, second)
  }

  @Test
  fun getLocalUser_returnsEarliestCreatedUser() = runTest {
    val dao = db.getUserDao()
    dao.insert(UserEntity("b", createdAt = 2))
    dao.insert(UserEntity("a", createdAt = 1))

    assertEquals("a", dao.getLocalUser()?.id)
  }

  @Test
  fun insertUsage_forMissingReading_throws() = runTest {
    val user = createUser()

    assertFailsWith<Exception> {
      db.getReadingUsageDao().insertUsage(ReadingUsageEntity("u1", user.id, "missing", usedAt = 1))
    }
  }

  @Test
  fun insertReadingWithTokens_returnsTokensInOrder() = runTest {
    val user = createUser()
    val dao = db.getReadingDao()

    dao.insertReadingWithTokens(
      reading("r1"),
      listOf(token("r1", 1, "は"), token("r1", 0, "私"), token("r1", 2, "学生")),
    )

    val tokens = dao.getTokens("r1")
    assertEquals(listOf("私", "は", "学生"), tokens.map { it.surface })
    assertEquals(listOf("名詞", "普通名詞"), tokens[0].partOfSpeech)
    assertEquals(user.id, dao.observeReadingsWithTokens(user.id).first().single().reading.userId)
  }

  @Test
  fun observeReadingsWithTokens_returnsOnlyThatUsersReadingsNewestFirst() = runTest {
    createUser("user-1")
    db.getUserDao().insert(UserEntity("user-2", createdAt = 0))
    val dao = db.getReadingDao()

    dao.insertReadingWithTokens(reading("old", createdAt = 1), listOf(token("old", 0)))
    dao.insertReadingWithTokens(reading("new", createdAt = 2), listOf(token("new", 0)))
    dao.insertReadingWithTokens(reading("other", "user-2"), emptyList())

    val result = dao.observeReadingsWithTokens("user-1").first()
    assertEquals(listOf("new", "old"), result.map { it.reading.id })
    assertEquals(1, result[0].tokens.size)
  }

  @Test
  fun deleteReading_cascadesToTokensAndUsage() = runTest {
    val user = createUser()
    val dao = db.getReadingDao()
    val usageDao = db.getReadingUsageDao()
    dao.insertReadingWithTokens(reading("r1"), listOf(token("r1", 0)))
    usageDao.insertUsage(ReadingUsageEntity("u1", user.id, "r1", usedAt = 10))

    dao.deleteById("r1")

    assertEquals(emptyList(), dao.getTokens("r1"))
    assertEquals(0, usageDao.getUsageCount("r1").first())
    assertEquals(emptyList(), dao.observeReadingsWithTokens(user.id).first())
  }

  @Test
  fun usage_tracksCountAndLastUsed() = runTest {
    val user = createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    val usageDao = db.getReadingUsageDao()
    assertNull(usageDao.getLastUsedTimestamp("r1"))

    usageDao.insertUsage(ReadingUsageEntity("u1", user.id, "r1", usedAt = 10))
    usageDao.insertUsage(ReadingUsageEntity("u2", user.id, "r1", usedAt = 20))

    assertEquals(2, usageDao.getUsageCount("r1").first())
    assertEquals(20L, usageDao.getLastUsedTimestamp("r1"))
    assertEquals(listOf("u2", "u1"), usageDao.getUsageHistoryForReading("r1").first().map { it.id })
  }

  @Test
  fun observeReadingSummaries_fetchesOrderedReadingsBasedOnUserId() = runTest {
    createUser("user-1")
    db.getUserDao().insert(UserEntity("user-2", createdAt = 0))
    val dao = db.getReadingDao()

    dao.insertReadingWithTokens(
      reading("old", createdAt = 1),
      listOf(
        token("old", 0),
        ReadingTokenEntity(
          readingId = "old",
          tokenIndex = 1,
          surface = "家",
          partOfSpeech = listOf("noun"),
          dictionaryForm = "いえ",
          pronunciation = "いえ",
        ),
      ),
    )
    dao.insertReadingWithTokens(reading("new", createdAt = 2), emptyList())
    dao.insertReadingWithTokens(reading("other", "user-2"), emptyList())

    val result = dao.observeReadingSummaries(userId = "user-1").first()

    assertEquals(listOf("new" to 0, "old" to 2), result.map { it.id to it.tokenCount })
  }
}
