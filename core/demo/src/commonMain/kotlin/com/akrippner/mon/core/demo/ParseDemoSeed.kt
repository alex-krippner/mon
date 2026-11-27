package com.akrippner.mon.core.demo

/** Reads the seed file the seed tool wrote. */
fun parseDemoSeed(json: String): DemoSeed =
  DemoSeedJson.decodeFromString(DemoSeed.serializer(), json)
