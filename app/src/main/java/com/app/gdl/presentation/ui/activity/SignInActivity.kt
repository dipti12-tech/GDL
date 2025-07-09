package com.app.gdl.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.databinding.ActivityLoginBinding
import com.app.gdl.domain.repository.LoginRepository
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
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM_TOKEN", "FCM Token: $token")
            fcm_token= "$token"
        }
        binding.btnSignIn.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this, "No internet. Please try later.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email = binding.etEmailMobile.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all the required fields to proceed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginViewModel.loginUser(LoginRequest(email, password,fcm_token))

            loginViewModel.loginResponse.observe(this) { response ->
                Log.d("loginResponse", "RESPONSE LOGIN: "+response.toString()+"SIZE"+response.customer_details?.email_id)
                if (response.status == 1) {
                    // Save login data
                    prefs.isLoggedIn = true
                    prefs.name =
                        (response.customer_details?.first_name + response.customer_details?.last_name)
                            ?: ""
                    prefs.mobile = response.customer_details?.phone ?: ""
                    prefs.userAdrress = (response.customer_details?.address?.get(0)?.text?: "").toString()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("addressUser", prefs.userAdrress)
                    intent.putExtra("from","SignIn")
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