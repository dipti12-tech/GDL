package com.app.gdl.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.databinding.ActivityLoginBinding
import com.app.gdl.presentation.viewmodel.LoginViewModel
import com.app.gdl.utils.NetworkUtils
import com.app.gdl.utils.SharedPref
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var fcm_token: String = ""
    lateinit var prefs : SharedPref

    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = SharedPref(this)

        binding.btnSignIn.setOnClickListener {

            val email = binding.etEmailMobile.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this, "No internet. Please try later.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                Log.d("FCM_TOKEN", "FCM Token: $token")
                fcm_token= token
            }
            loginViewModel.loginUser(LoginRequest(email, password,fcm_token))

            loginViewModel.loginResponse.observe(this) { response ->
                if (response.status == 1) {
                    prefs.isLoggedIn = true
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, response.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvRegisterNow.setOnClickListener{
            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}