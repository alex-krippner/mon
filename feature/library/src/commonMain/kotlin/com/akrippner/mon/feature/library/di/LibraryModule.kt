package com.akrippner.mon.feature.library.di

import com.akrippner.mon.feature.library.LibraryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val libraryModule = module { viewModelOf(::LibraryViewModel) }
