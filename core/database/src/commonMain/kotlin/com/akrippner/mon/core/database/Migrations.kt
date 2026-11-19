package com.akrippner.mon.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Readings remember which tokenizer produced their tokens, and a reading without one is waiting for
 * its tokens. Every reading that exists at this point was tokenized by the bundled Sudachi
 * dictionary, so it is marked as done.
 */
internal val MIGRATION_2_3 =
  object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
      connection.execSQL("ALTER TABLE reading ADD COLUMN tokenizedWith TEXT")
      connection.execSQL("UPDATE reading SET tokenizedWith = '$BUNDLED_TOKENIZER_ID'")
    }
  }

/** The `Tokenizer.id` of the Sudachi dictionary that used to ship inside the app. */
internal const val BUNDLED_TOKENIZER_ID = "sudachi-bundled"

/**
 * Migrations between schema versions, starting at version 2. When you bump the [MonDatabase]
 * version, add a migration here (or an `AutoMigration` on [MonDatabase]) and a test in
 * `MonDatabaseMigrationTest`. Without one, opening an existing database fails instead of losing the
 * user's data.
 */
internal val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_2_3)
