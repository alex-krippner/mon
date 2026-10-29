package com.akrippner.mon.core.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.akrippner.mon.core.database.entity.UserEntity
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

/**
 * Opens databases created from the exported schemas (`core/database/schemas`) with the app's real
 * configuration ([getRoomDatabase]), to check that upgrades keep the user's data.
 */
class MonDatabaseMigrationTest {
  private val tempDir: Path = Files.createTempDirectory("mon-migration")
  private val dbPath: Path = tempDir.resolve("mon.db")

  private val helper =
    MigrationTestHelper(
      schemaDirectoryPath = Path.of("schemas"),
      databasePath = dbPath,
      driver = BundledSQLiteDriver(),
      databaseClass = MonDatabase::class,
      databaseFactory = MonDatabaseConstructor::initialize,
    )

  @OptIn(kotlin.io.path.ExperimentalPathApi::class)
  @AfterTest
  fun tearDown() {
    tempDir.deleteRecursively()
  }

  private fun openAppDatabase(): MonDatabase =
    getRoomDatabase(
      Room.databaseBuilder<MonDatabase>(
        name = dbPath.toString(),
        factory = MonDatabaseConstructor::initialize,
      )
    )

  @Test
  fun version2_keepsReadingsWhenOpened() = runTest {
    helper.createDatabase(2).use { connection ->
      connection.execSQL("INSERT INTO user (id, createdAt) VALUES ('user-1', 0)")
      connection.execSQL(
        "INSERT INTO reading (id, userId, title, text, translation, sourceLanguage, " +
          "translationLanguage, createdAt, updatedAt) " +
          "VALUES ('reading-1', 'user-1', 'Title', 'こんにちは', 'Hello', 'ja', 'en', 1, 1)"
      )
      connection.execSQL(
        "INSERT INTO reading_token (readingId, tokenIndex, surface, pronunciation, " +
          "dictionaryForm, partOfSpeech) VALUES ('reading-1', 0, 'こんにちは', 'コンニチハ', " +
          "'こんにちは', '感動詞')"
      )
    }

    val db = openAppDatabase()
    try {
      val readings = db.getReadingDao().observeReadingsWithTokens("user-1").first()
      assertEquals(listOf("reading-1"), readings.map { it.reading.id })
      assertEquals(listOf("こんにちは"), readings.single().tokens.map { it.surface })
    } finally {
      db.close()
    }
  }

  @Test
  fun version1_isRecreatedWhenOpened() = runTest {
    helper.createDatabase(1).use { connection ->
      connection.execSQL(
        "INSERT INTO ReadingEntity (text, translation, title) VALUES ('こんにちは', 'Hello', 'Title')"
      )
    }

    val db = openAppDatabase()
    try {
      val user = db.getUserDao().getOrCreateLocalUser(UserEntity(id = "user-1", createdAt = 0))
      assertTrue(db.getReadingDao().observeReadingsWithTokens(user.id).first().isEmpty())
    } finally {
      db.close()
    }
  }
}
