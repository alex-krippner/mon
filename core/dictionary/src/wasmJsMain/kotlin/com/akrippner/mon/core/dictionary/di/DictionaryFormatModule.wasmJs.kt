package com.akrippner.mon.core.dictionary.di

// The JMdict reader needs native SQLite and files, so the browser registers no dictionary format.
internal actual val dictionaryFormatModule = org.koin.dsl.module {}
