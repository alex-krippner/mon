package com.akrippner.mon.feature.addreading.file

/**
 * A file the reader chose or dropped, before anything is read from it. [size] is null when the
 * platform cannot say. [readBytes] does the read and moves to a background dispatcher itself, so
 * callers may call it from any coroutine.
 */
class PickedFile(val name: String, val size: Long?, val readBytes: suspend () -> ByteArray)
