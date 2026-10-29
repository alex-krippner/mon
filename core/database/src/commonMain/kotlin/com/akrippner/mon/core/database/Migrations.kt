package com.akrippner.mon.core.database

import androidx.room.migration.Migration

/**
 * Migrations between schema versions, starting at version 2. When you bump the [MonDatabase]
 * version, add a migration here (or an `AutoMigration` on [MonDatabase]) and a test in
 * `MonDatabaseMigrationTest`. Without one, opening an existing database fails instead of losing the
 * user's data.
 */
internal val MIGRATIONS: Array<Migration> = emptyArray()
