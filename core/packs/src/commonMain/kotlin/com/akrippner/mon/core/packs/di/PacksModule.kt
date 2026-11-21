package com.akrippner.mon.core.packs.di

import org.koin.core.module.Module

/**
 * Provides the [com.akrippner.mon.core.packs.PackManager]. It reads every file format registered in
 * Koin as a [com.akrippner.mon.core.packs.PackFormat], so the modules that own a format only have
 * to register it.
 */
expect val packsModule: Module
