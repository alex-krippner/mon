package com.akrippner.mon.core.dictionary.pack

import java.io.IOException

internal class PackException(val error: PackError) : RuntimeException(error.name)

/** Runs [block], turning an IOException into a STORAGE error. */
internal inline fun <T> storage(block: () -> T): T =
  try {
    block()
  } catch (e: IOException) {
    throw PackException(PackError.STORAGE)
  }
