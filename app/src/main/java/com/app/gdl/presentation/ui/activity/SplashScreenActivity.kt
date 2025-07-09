package com.app.gdl.presentation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.R
import com.app.gdl.utils.SharedPref

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = SharedPref(this)

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("TAG", "onCreate: "+prefs.isLoggedIn)
          /*  if (prefs.isLoggedIn) {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("addressUser", prefs.userAdrress)
                intent.putExtra("from", "splash")

                startActivity(intent)
            } else {
                startActivity(Intent(this, SignUpActivity::class.java))
            }*/
            //for default user
            startActivity(Intent(this, MainActivity::class.java))
            intent.putExtra("addressUser", "")
            intent.putExtra("from", "splash")
            finish()
        }, 500)
    }
}