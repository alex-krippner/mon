package com.akrippner.mon.core.database.converter

import androidx.room.TypeConverter

class StringListConverter {
  @TypeConverter
  fun fromStringList(value: List<String>): String = value.joinToString(PART_SEPARATOR)

  @TypeConverter
  fun toStringList(value: String): List<String> =
    if (value.isEmpty()) {
      emptyList()
    } else {
      value.split(PART_SEPARATOR)
    }

  private companion object {
    const val PART_SEPARATOR = "\u001F"
  }
}
