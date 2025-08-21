package com.app.gdl.presentation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.R
import com.app.gdl.utils.AnalyticsHelper
import com.google.firebase.analytics.FirebaseAnalytics

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AnalyticsHelper.logScreenView(this, "Splash Screen")

        Handler(Looper.getMainLooper()).postDelayed({
            //for Guest user
            startActivity(Intent(this, MainActivity::class.java))
            intent.putExtra("addressUser", "")
            intent.putExtra("from", "splash")
            finish()
        }, 500)
    }
}