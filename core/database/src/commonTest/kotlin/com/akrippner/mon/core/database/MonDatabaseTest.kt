package com.akrippner.mon.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akrippner.mon.core.database.entity.ReadingEntity
import com.akrippner.mon.core.database.entity.ReadingTokenEntity
import com.akrippner.mon.core.database.entity.ReadingUsageEntity
import com.akrippner.mon.core.database.entity.ReadingWithTokens
import com.akrippner.mon.core.database.entity.SavedWordEntity
import com.akrippner.mon.core.database.entity.UserEntity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

  private fun reading(
    id: String,
    userId: String = "user-1",
    createdAt: Long = 0,
    tokenizedWith: String? = "test-tokenizer",
  ) =
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
      tokenizedWith = tokenizedWith,
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

  @Test
  fun getPendingReadings_returnsOnlyUntokenizedReadingsOfTheUserOldestFirst() = runTest {
    createUser("user-1")
    db.getUserDao().insert(UserEntity("user-2", createdAt = 0))
    val dao = db.getReadingDao()
    dao.insertReadingWithTokens(reading("new", createdAt = 2, tokenizedWith = null), emptyList())
    dao.insertReadingWithTokens(reading("old", createdAt = 1, tokenizedWith = null), emptyList())
    dao.insertReadingWithTokens(reading("done", createdAt = 0), listOf(token("done", 0)))
    dao.insertReadingWithTokens(reading("other", "user-2", tokenizedWith = null), emptyList())

    assertEquals(listOf("old", "new"), dao.getPendingReadings("user-1").map { it.id })
  }

  @Test
  fun setTokens_givesAPendingReadingItsTokens() = runTest {
    createUser()
    val dao = db.getReadingDao()
    dao.insertReadingWithTokens(reading("r1", tokenizedWith = null), emptyList())

    val saved =
      dao.setTokens("r1", listOf(token("r1", 1, "は"), token("r1", 0, "私")), "sudachi-core@1")

    assertTrue(saved)
    assertEquals(listOf("私", "は"), dao.getTokens("r1").map { it.surface })
    assertEquals("sudachi-core@1", dao.getReadingWithTokens("r1")?.reading?.tokenizedWith)
    assertEquals(emptyList(), dao.getPendingReadings("user-1"))
  }

  @Test
  fun setTokens_isIdempotent_theSecondCallChangesNothing() = runTest {
    createUser()
    val dao = db.getReadingDao()
    dao.insertReadingWithTokens(reading("r1", tokenizedWith = null), emptyList())

    assertTrue(dao.setTokens("r1", listOf(token("r1", 0, "私")), "first"))
    assertFalse(dao.setTokens("r1", listOf(token("r1", 0, "あなた")), "second"))

    assertEquals(listOf("私"), dao.getTokens("r1").map { it.surface })
    assertEquals("first", dao.getReadingWithTokens("r1")?.reading?.tokenizedWith)
  }

  @Test
  fun observeReadingWithTokens_emitsAgainWhenAPendingReadingGetsItsTokens() = runTest {
    createUser()
    val dao = db.getReadingDao()
    dao.insertReadingWithTokens(reading("r1", tokenizedWith = null), emptyList())

    // One collector stays subscribed: collecting a fresh flow twice would only query twice and
    // prove nothing about Room telling an existing collector about the change.
    val emissions = Channel<ReadingWithTokens?>(Channel.UNLIMITED)
    val collector = launch { dao.observeReadingWithTokens("r1").collect { emissions.send(it) } }

    val pending = emissions.receive()
    assertEquals(emptyList(), pending?.tokens)
    assertNull(pending?.reading?.tokenizedWith)

    dao.setTokens("r1", listOf(token("r1", 0, "私")), "sudachi-core@1")

    val tokenized = emissions.receiveMatching { it?.tokens?.isNotEmpty() == true }
    assertEquals(listOf("私"), tokenized?.tokens?.map { it.surface })
    assertEquals("sudachi-core@1", tokenized?.reading?.tokenizedWith)
    collector.cancel()
  }

  @Test
  fun observeReadingWithTokens_emitsNullWhenTheReadingIsDeleted() = runTest {
    createUser()
    val dao = db.getReadingDao()
    dao.insertReadingWithTokens(reading("r1"), listOf(token("r1", 0)))

    val emissions = Channel<ReadingWithTokens?>(Channel.UNLIMITED)
    val collector = launch { dao.observeReadingWithTokens("r1").collect { emissions.send(it) } }
    assertEquals(1, emissions.receive()?.tokens?.size)

    dao.deleteById("r1")

    assertNull(emissions.receiveMatching { it == null })
    collector.cancel()
  }

  /** Skips emissions until one matches: Room may emit more than once for one change. */
  private suspend fun <T> Channel<T>.receiveMatching(predicate: (T) -> Boolean): T {
    while (true) {
      val value = receive()
      if (predicate(value)) return value
    }
  }

  @Test
  fun observeReadingWithTokens_ofAMissingReading_emitsNull() = runTest {
    assertNull(db.getReadingDao().observeReadingWithTokens("missing").first())
  }

  @Test
  fun setTokens_ofAMissingReading_returnsFalse() = runTest {
    createUser()

    assertFalse(db.getReadingDao().setTokens("missing", emptyList(), "x"))
  }

  @Test
  fun observeReadingSummaries_saysWhetherAReadingIsTokenized() = runTest {
    createUser()
    val dao = db.getReadingDao()
    dao.insertReadingWithTokens(
      reading("pending", createdAt = 2, tokenizedWith = null),
      emptyList(),
    )
    dao.insertReadingWithTokens(reading("done", createdAt = 1), listOf(token("done", 0)))

    val summaries = dao.observeReadingSummaries("user-1").first()

    assertEquals(
      listOf("pending" to false, "done" to true),
      summaries.map { it.id to it.isTokenized },
    )
  }

  private fun savedWord(
    id: String,
    readingId: String = "r1",
    dictionaryForm: String = "先生",
    textOffset: Int = 0,
    wordStart: Int = 0,
    savedAt: Long = 0,
    note: String? = null,
  ) =
    SavedWordEntity(
      id = id,
      readingId = readingId,
      dictionaryForm = dictionaryForm,
      surface = dictionaryForm,
      kana = "せんせい",
      gloss = "teacher",
      jlpt = null,
      jmdictSeq = 1_000_000L,
      sentenceText = "私は先生と呼んだ。",
      wordStart = wordStart,
      wordEnd = wordStart + dictionaryForm.length,
      textOffset = textOffset,
      note = note,
      savedAt = savedAt,
    )

  @Test
  fun saveIfAbsent_savesAWordOncePerReading_andReturnsTheStoredOne() = runTest {
    createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    db.getReadingDao().insertReadingWithTokens(reading("r2"), emptyList())
    val dao = db.getSavedWordDao()

    val first = dao.saveIfAbsent(savedWord("w1", note = "first"))
    val again = dao.saveIfAbsent(savedWord("w2", note = "second"))
    val otherReading = dao.saveIfAbsent(savedWord("w3", readingId = "r2"))

    assertEquals("w1", first.id)
    assertEquals("w1", again.id)
    assertEquals("first", again.note)
    assertEquals("w3", otherReading.id)
    assertEquals(1, dao.observeCount("r1").first())
    assertEquals(1, dao.observeCount("r2").first())
  }

  @Test
  fun observeForReading_ordersWordsByTheirPlaceInTheText() = runTest {
    createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    val dao = db.getSavedWordDao()
    dao.saveIfAbsent(savedWord("late", dictionaryForm = "夏", textOffset = 20, wordStart = 3))
    dao.saveIfAbsent(savedWord("early", dictionaryForm = "記憶", textOffset = 0, wordStart = 5))
    dao.saveIfAbsent(savedWord("middle", dictionaryForm = "鎌倉", textOffset = 10, wordStart = 1))

    assertEquals(
      listOf("early", "middle", "late"),
      dao.observeForReading("r1").first().map { it.id },
    )
  }

  @Test
  fun deleteReading_cascadesToItsSavedWords() = runTest {
    createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    val dao = db.getSavedWordDao()
    dao.saveIfAbsent(savedWord("w1"))

    db.getReadingDao().deleteById("r1")

    assertNull(dao.getById("w1"))
    assertEquals(0, dao.observeCount("r1").first())
  }

  @Test
  fun remove_returnsTheWord_andInsertOrReplaceRestoresItWithItsNote() = runTest {
    createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    val dao = db.getSavedWordDao()
    dao.saveIfAbsent(savedWord("w1"))
    dao.setNote("w1", "memory themes")

    val removed = dao.remove("w1")

    assertEquals("memory themes", removed?.note)
    assertNull(dao.getById("w1"))
    assertNull(dao.remove("w1"))

    dao.insertOrReplace(removed!!)
    assertEquals("memory themes", dao.getById("w1")?.note)
  }

  @Test
  fun insertOrReplace_replacesAWordSavedAgainInTheMeantime() = runTest {
    createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    val dao = db.getSavedWordDao()
    val removed = dao.saveIfAbsent(savedWord("old", note = "kept"))
    dao.remove("old")
    dao.saveIfAbsent(savedWord("new"))

    dao.insertOrReplace(removed)

    assertEquals(listOf("old"), dao.observeForReading("r1").first().map { it.id })
  }

  @Test
  fun observeByForm_followsSavingAndRemoving() = runTest {
    createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    val dao = db.getSavedWordDao()
    assertNull(dao.observeByForm("r1", "先生").first())

    dao.saveIfAbsent(savedWord("w1"))
    assertEquals("w1", dao.observeByForm("r1", "先生").first()?.id)

    dao.remove("w1")
    assertNull(dao.observeByForm("r1", "先生").first())
  }

  @Test
  fun observeCount_updatesForACollectorThatStaysSubscribed() = runTest {
    createUser()
    db.getReadingDao().insertReadingWithTokens(reading("r1"), emptyList())
    val dao = db.getSavedWordDao()
    val counts = Channel<Int>(Channel.UNLIMITED)
    val collector = launch { dao.observeCount("r1").collect { counts.send(it) } }
    assertEquals(0, counts.receive())

    dao.saveIfAbsent(savedWord("w1"))

    assertEquals(1, counts.receiveMatching { it == 1 })
    collector.cancel()
  }

  @Test
  fun observeGroupRows_listsTheUsersWordsByLastOpenedReading_newestWordFirst() = runTest {
    val user = createUser()
    db.getUserDao().insert(UserEntity("user-2", createdAt = 0))
    val readings = db.getReadingDao()
    readings.insertReadingWithTokens(reading("never", createdAt = 5), emptyList())
    readings.insertReadingWithTokens(reading("opened", createdAt = 1), emptyList())
    readings.insertReadingWithTokens(reading("stale", createdAt = 2), emptyList())
    readings.insertReadingWithTokens(reading("other", "user-2", createdAt = 9), emptyList())
    db.getReadingUsageDao().insertUsage(ReadingUsageEntity("u1", user.id, "opened", usedAt = 50))
    val dao = db.getSavedWordDao()
    dao.saveIfAbsent(savedWord("a", dictionaryForm = "夏", readingId = "never", savedAt = 1))
    dao.saveIfAbsent(savedWord("b", dictionaryForm = "記憶", readingId = "opened", savedAt = 1))
    dao.saveIfAbsent(savedWord("c", dictionaryForm = "鎌倉", readingId = "opened", savedAt = 2))
    dao.saveIfAbsent(savedWord("d", dictionaryForm = "先生", readingId = "stale", savedAt = 1))
    dao.saveIfAbsent(savedWord("x", dictionaryForm = "政府", readingId = "other", savedAt = 1))

    val rows = dao.observeGroupRows(user.id).first()

    // "opened" was opened last (50), then "never" counts from its creation (5), then "stale" (2).
    assertEquals(listOf("c", "b", "a", "d"), rows.map { it.word.id })
    assertEquals(
      listOf("Title opened", "Title opened", "Title never", "Title stale"),
      rows.map { it.readingTitle },
    )
    assertEquals(listOf(50L, 50L, 5L, 2L), rows.map { it.lastOpenedAt })
  }
}
