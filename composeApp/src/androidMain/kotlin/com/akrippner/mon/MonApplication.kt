package com.akrippner.mon

import android.app.Application
import org.koin.android.ext.koin.androidContext

class MonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MonApplication)
        }
    }
}
