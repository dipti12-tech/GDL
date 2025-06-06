package com.app.gdl

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GilanisApplication :Application() {

    override fun onCreate() {
        super.onCreate()

    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}