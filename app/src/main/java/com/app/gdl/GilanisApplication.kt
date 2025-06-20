package com.app.gdl

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GilanisApplication :Application() {

    override fun onCreate() {
        super.onCreate()
      //  FirebaseApp.initializeApp(this)--- exceptional
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}