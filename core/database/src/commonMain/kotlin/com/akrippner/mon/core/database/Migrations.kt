package com.akrippner.mon.core.database

import androidx.room.migration.Migration

/**
 * Migrations between schema versions. The app is not released yet, so a schema change edits version
 * 1 in place and there are none. After the first release, bump the [MonDatabase] version for every
 * schema change and add a migration here (or an `AutoMigration` on [MonDatabase]) and a test in
 * `MonDatabaseMigrationTest`. Without one, opening an existing database fails instead of losing the
 * user's data.
 */
internal val MIGRATIONS: Array<Migration> = emptyArray()
