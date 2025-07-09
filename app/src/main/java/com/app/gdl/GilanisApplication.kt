package com.app.gdl

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GilanisApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        CartManager.init(this)
        //  FirebaseApp.initializeApp(this)--- exceptional
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}