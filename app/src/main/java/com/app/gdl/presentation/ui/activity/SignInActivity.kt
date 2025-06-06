package com.app.gdl.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.databinding.ActivityLoginBinding
import com.app.gdl.presentation.viewmodel.LoginViewModel
import com.app.gdl.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
            loginViewModel.loginUser(LoginRequest(email, password))

            loginViewModel.loginResponse.observe(this) { response ->
                if (response.status == 1) {
                    //   Toast.makeText(this, response.msg, Toast.LENGTH_SHORT).show()
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, response.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}