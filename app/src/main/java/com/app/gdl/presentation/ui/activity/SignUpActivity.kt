package com.app.gdl.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.data.model.Address
import com.app.gdl.data.model.SignupRequest
import com.app.gdl.databinding.ActivitySignupBinding
import com.app.gdl.presentation.viewmodel.SignupViewModel
import com.app.gdl.utils.NetworkUtils
import com.app.gdl.utils.SharedPref
import com.google.android.material.chip.Chip
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val signupViewModel: SignupViewModel by viewModels()

    private val saveasAddress = listOf("Home", "Work", "Hotel", "Other")
    private var addressType: String = ""

    private var finalAddress: String? = null
    private var headingAddress: String? = null
    private var cityin: String? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var lat: Double = 0.0
    var lng: Double = 0.0
    private var fcm_token: String = ""
    var isPasswordVisible = false
    lateinit var prefs: SharedPref
    private val mapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val address = it.data?.getStringExtra("address")
                headingAddress = it.data?.getStringExtra("headingAddress")
                finalAddress = address
                cityin = it.data?.getStringExtra("city")
                binding.etAddress.text = headingAddress
                addressType = it.data?.getStringExtra("addressType").toString()
                lat = it.data?.getDoubleExtra("lat", 0.0) ?: 0.0
                lng = it.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            }
        }

    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = SharedPref(this)

        binding.ivTogglePassword.load("file:///android_asset/eye.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        binding.ivToggleConfirmpassword.load("file:///android_asset/eye.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM_TOKEN", "FCM Token: $token")
            fcm_token = token
        }

        askPermission()
        setupAddressChips(saveasAddress)

        binding.btnSignIn.setOnClickListener {
            intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.etEmail.setText("+254 ")
        binding.etEmail.setSelection(binding.etEmail.text.length)

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().startsWith("+254 ")) {
                    binding.etEmail.setText("+254 ")
                    binding.etEmail.setSelection(binding.etEmail.text.length)
                }

                if (s?.length == 14) {  // +254 + space + 9 digits = 14 characters
                    binding.etAddress.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        binding.etAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    binding.etFirstName.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.etAddress.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            mapResultLauncher.launch(intent)
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
        binding.ivToggleConfirmpassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                binding.etConfirmPassword.transformationMethod = null
                binding.ivToggleConfirmpassword.load("file:///android_asset/eye_off.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            } else {
                binding.etConfirmPassword.transformationMethod =
                    android.text.method.PasswordTransformationMethod.getInstance()
                binding.ivToggleConfirmpassword.load("file:///android_asset/eye.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
        }

        binding.btnCreateAccount.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this, "No internet. Please try later.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val rawInput = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val addressText = binding.etAddress.text.toString().trim()

            if (firstName.isEmpty()) {
                binding.etFirstName.error = "This is a required field"
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {
                binding.etLastName.error = "This is a required field"
                return@setOnClickListener
            }

            if (rawInput.isEmpty()) {
                binding.etEmail.error = "This is a required field"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "This is a required field", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }

            if (password != confirmPassword) {

                Toast.makeText(this, "Your Passwords DO Not Match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (addressText.isEmpty()) {
                binding.etAddress.error = "This is a required field"
                return@setOnClickListener
            }

            val default = if (addressType.equals("Home", ignoreCase = true)) 1 else 0
            val email = if (binding.etEmail.text.toString().trim().startsWith("+254 ")) {
                rawInput.removePrefix("+254 ").trim()
            } else {
                rawInput
            }
            Log.d("rawInput", "onCreate: " + email)
            val request = SignupRequest(
                first_name = firstName,
                last_name = lastName,
                email_id = "",
                phone = "254" + email,
                password = prefs.md5Hash(password),
                fcm_token = fcm_token,
                address = listOf(
                    Address(
                        name = addressType,
                        text = addressText,
                        city = cityin.toString(),
                        map_location = listOf(lat, lng),
                        default = default
                    )
                )
            )
            signupViewModel.signup(request)
        }

        signupViewModel.signupResult.observe(this) { result ->
            result.onSuccess { response ->
                if (response.status == 1) {
                    val customer = response.customer_details
                    customer?.let { prefs.saveCustomerDetailsToPrefs(this, it) }

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("addressUser", binding.etAddress.text.toString())
                    intent.putExtra("from", "SignUp")

                    prefs.userAdrress = binding.etAddress.text.toString()
                    prefs.name = "${binding.etFirstName.text} ${binding.etLastName.text}"
                    prefs.mobile = binding.etEmail.text.toString()
                    prefs.custid = customer?.customer_id.toString()
                    prefs.isLoggedIn = true

                    prefs.default_price = customer?.address?.get(0)?.price_class
                    prefs.near_warehouse = customer?.address?.get(0)?.warehouse
                    prefs.cityForOrder = cityin

                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(intent)
                        finish()
                    }, 300)
                } else {
                    Toast.makeText(this, response.message ?: "Signup failed", Toast.LENGTH_LONG)
                        .show()
                }
            }.onFailure {
                Toast.makeText(this, "Signup Failed: ${it.localizedMessage}", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }

    private fun setupAddressChips(saveasAddress: List<String>) {
        binding.categoryGroup?.removeAllViews()

        for (name in saveasAddress) {
            val chip = Chip(this).apply {
                text = name
                isCheckable = true
                isClickable = true
                isCheckedIconVisible = false
                chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                chipStrokeWidth = 2f
                chipStrokeColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chip.chipBackgroundColor =
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bg_chips))
                    chip.setTextColor(ContextCompat.getColor(this, R.color.white))
                    chip.chipStrokeWidth = 2f
                    chip.chipStrokeColor = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.bg_createaccount
                        )
                    )
                } else {
                    chip.chipBackgroundColor =
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                    chip.setTextColor(ContextCompat.getColor(this, R.color.black))
                    chip.chipStrokeWidth = 2f
                    chip.chipStrokeColor =
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
                }
            }

            binding.categoryGroup?.addView(chip)
        }


        binding.categoryGroup?.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            chip?.let {
                addressType = "${chip.text}"
            }
        }
    }

    private fun askPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

    }
}