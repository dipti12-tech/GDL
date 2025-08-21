package com.app.gdl.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.decode.SvgDecoder
import coil.load
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
    lateinit var prefs: SharedPref
    var isPasswordVisible = false

    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = SharedPref(this)
        binding.ivTogglePassword.load("file:///android_asset/eye.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM_TOKEN", "FCM Token: $token")
            fcm_token = "$token"
        }
        binding.etEmailMobile.setText("+254 ")  // Set initial value with country code
        binding.etEmailMobile.setSelection(binding.etEmailMobile.text.length)  // Move cursor to end

        binding.etEmailMobile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().startsWith("+254 ")) {
                    binding.etEmailMobile.setText("+254 ")
                    binding.etEmailMobile.setSelection(binding.etEmailMobile.text.length)
                }

                if (s?.length == 14) {  // +254 + space + 9 digits = 14 characters
                    binding.etPassword.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        loginViewModel.loginResponse.observe(this) { response ->

            if (response.status == 1) {
                Log.d("LOGIN RESPONSE", "onCreate: " + response.customer_details)
                val customer = response.customer_details
                customer?.let { prefs.saveCustomerDetailsToPrefs(this, it) }
                prefs.isLoggedIn = true
                prefs.name =
                    (response.customer_details?.first_name + " " + response.customer_details?.last_name)
                        ?: ""
                prefs.mobile = response.customer_details?.phone ?: ""
                prefs.custid = response.customer_details?.customer_id.toString()
                prefs.userAdrress =
                    (response.customer_details?.address?.get(0)?.text ?: "").toString()
                prefs.default_price = customer?.address?.get(0)?.price_class.toString()
                prefs.near_warehouse = customer?.address?.get(0)?.warehouse.toString()
                prefs.citySelected = true
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("addressUser", prefs.userAdrress)
                intent.putExtra("custId", prefs.custid?.toInt())
                intent.putExtra("from", "SignIn")
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(intent)
                    finish()
                }, 300)
            } else {
                Toast.makeText(this, response.msg, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSignIn.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this, "No internet. Please try later.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val rawInput = binding.etEmailMobile.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val email = if (rawInput.startsWith("+254 ")) {
                rawInput.removePrefix("+254 ").trim()
            } else {
                rawInput
            }
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please fill in all the required fields to proceed",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            loginViewModel.loginUser(
                LoginRequest(
                    "254" + email,
                    prefs.md5Hash(password),
                    fcm_token
                )
            )

        }

        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                binding.etPassword.transformationMethod = null
                binding.ivTogglePassword.load("file:///android_asset/eye_off.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            } else {
                binding.etPassword.transformationMethod =
                    android.text.method.PasswordTransformationMethod.getInstance()
                binding.ivTogglePassword.load("file:///android_asset/eye.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            }

            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
        binding.tvRegisterNow.setOnClickListener {
            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}