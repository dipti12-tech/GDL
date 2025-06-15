package com.app.gdl.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.R
import com.app.gdl.utils.SharedPref

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = SharedPref(this)

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("TAG", "onCreate: "+prefs.isLoggedIn)
            if (prefs.isLoggedIn==true) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            finish()
        }, 500) // 1.5 second delay
    }
}