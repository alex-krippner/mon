package com.akrippner.mon.core.data.internal

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class) internal fun newId(): String = Uuid.random().toString()

internal fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
