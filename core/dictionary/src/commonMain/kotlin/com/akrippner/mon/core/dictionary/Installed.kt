package com.akrippner.mon.core.dictionary

import com.akrippner.mon.core.packs.PackManager

/** The installed packs that can look words up. Another kind of reader adds its own extension. */
internal suspend fun PackManager.installed(): List<MonDictionary> =
  readers().filterIsInstance<MonDictionary>()
