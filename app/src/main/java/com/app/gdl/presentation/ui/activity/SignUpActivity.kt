package com.app.gdl.presentation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.data.model.Address
import com.app.gdl.data.model.SignupRequest
import com.app.gdl.databinding.ActivitySignupBinding
import com.app.gdl.presentation.ui.adapters.SearchSuggestionsAdapter
import com.app.gdl.presentation.viewmodel.SignupViewModel
import com.app.gdl.utils.NetworkUtils
import com.app.gdl.utils.SharedPref
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val signupViewModel: SignupViewModel by viewModels()

    private val saveasAddress = listOf("Home", "Work", "Hotel", "Other")
    private var addressType: String = ""

    //private var etStreetInBottomSheet: EditText? = null
    private var finalAddress: String? = null
    private var headingAddress: String? = null
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
                binding.etAddress.text = finalAddress
                addressType = it.data?.getStringExtra("addressType").toString()
                lat = it.data?.getDoubleExtra("lat", 0.0) ?: 0.0
                lng = it.data?.getDoubleExtra("lng", 0.0) ?: 0.0
                Log.d(
                    "NewAddressDetails",
                    "Signup Request: $finalAddress addressType $addressType lat $lat lng  $lng")
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
        setupPickLocation()

        binding.btnSignIn.setOnClickListener {
            intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 10) {
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

        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                binding.etPassword.transformationMethod = null
                //   binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off) // Change icon
                binding.ivTogglePassword.load("file:///android_asset/eye_off.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            } else {
                // Hide password
                binding.etPassword.transformationMethod =
                    android.text.method.PasswordTransformationMethod.getInstance()
                //      binding.ivTogglePassword.setImageResource(R.drawable.ic_eye) // Change icon
                binding.ivTogglePassword.load("file:///android_asset/eye.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            }

            // Move cursor to the end
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
        binding.ivToggleConfirmpassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                binding.etConfirmPassword.transformationMethod = null
                binding.ivToggleConfirmpassword.load("file:///android_asset/eye_off.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            } else {
                // Hide password
                binding.etConfirmPassword.transformationMethod =
                    android.text.method.PasswordTransformationMethod.getInstance()
                //  binding.ivToggleConfirmpassword.setImageResource(R.drawable.ic_eye) // Change icon
                binding.ivToggleConfirmpassword.load("file:///android_asset/eye.svg") {
                    decoderFactory(SvgDecoder.Factory())
                }
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
        }
        binding.btnCreateAccount.setOnClickListener {
            /* val password = binding.etPassword.text.toString()
             val confirmPassword = binding.etConfirmPassword.text.toString()

             if (password != confirmPassword) {
                 Toast.makeText(this, "Password is mismatch", Toast.LENGTH_LONG).show()
                 return@setOnClickListener
             }

             val default = if (addressType.equals("Home", ignoreCase = true)) 1 else 0

             val request = SignupRequest(
                 first_name = binding.etFirstName.text.toString(),
                 last_name = binding.etLastName.text.toString(),
                 email_id = binding.etEmail.text.toString(),
                 phone = binding.etEmail.text.toString(),  // Assuming phone is email? Consider using phone EditText instead
                 password = password,
                 fcm_token = fcm_token,
                 address = listOf(
                     Address(
                         name = addressType,
                         text = binding.etAddress.text.toString(),
                         map_location = listOf(latitude, longitude),
                         default = default
                     )
                 )
             )

             Log.d("requestFCM", "Signup Request: $request")
             signupViewModel.signup(request)
 */
            binding.btnCreateAccount.setOnClickListener {
                if (!NetworkUtils.isInternetAvailable(this)) {
                    Toast.makeText(this, "No internet. Please try later.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                val firstName = binding.etFirstName.text.toString().trim()
                val lastName = binding.etLastName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
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

                if (email.isEmpty()) {
                    binding.etEmail.error = "This is a required field"
                    return@setOnClickListener
                }

                /*  if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                      binding.etEmail.error = "Enter a valid email address"
                      return@setOnClickListener
                  }*/

                if (password.isEmpty()) {
                    /*  binding.etPassword.error = "Password is required"
                      return@setOnClickListener*/
                    Toast.makeText(this, "This is a required field", Toast.LENGTH_LONG).show()
                    return@setOnClickListener

                }

                if (confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please confirm your password", Toast.LENGTH_LONG).show()
                    return@setOnClickListener

                }

                if (password != confirmPassword) {
                    /*  binding.etConfirmPassword.error = "Passwords do not match"
                      return@setOnClickListener*/
                    Toast.makeText(this, "Your Passwords DO Not Match", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (addressText.isEmpty()) {
                    binding.etAddress.error = "This is a required field"
                    return@setOnClickListener
                }

                val default = if (addressType.equals("Home", ignoreCase = true)) 1 else 0

                val request = SignupRequest(
                    first_name = firstName,
                    last_name = lastName,
                    email_id = email,
                    phone = email,
                    password = password,
                    fcm_token = fcm_token,
                    address = listOf(
                        Address(
                            name = addressType,
                            text = addressText,
                            map_location = listOf(lat, lng),
                            default = default
                        )
                    )
                )

                Log.d("requestFCM", "Signup Request: $request")
                signupViewModel.signup(request)
            }

        }
        signupViewModel.signupResult.observe(this) { result ->
            result.onSuccess { response ->
                Log.d(
                    "SINGNuP RESPONSE: ", "RESPONSE: " + response.customer_id
                )
                if (response.status == 1) {
                    // Success case
                    Toast.makeText(this, response.message, Toast.LENGTH_LONG).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("addressUser", binding.etAddress.text.toString())
                    intent.putExtra("from", "SignUp")

                    prefs.userAdrress = binding.etAddress.text.toString()
                    prefs.name = "${binding.etFirstName.text} ${binding.etLastName.text}"
                    prefs.mobile = binding.etEmail.text.toString()
                    prefs.isLoggedIn = true

                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, response.message ?: "Signup failed", Toast.LENGTH_LONG)
                        .show()
                }
            }.onFailure {
                // Network error or exception
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
                //   Toast.makeText(this, "Selected Type: ${chip.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPickLocation() {
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_location_bottom_sheet, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)
        // val etSearch = bottomSheetView.findViewById<EditText>(R.id.etSearchLocation)
        // val etFlats = bottomSheetView.findViewById<EditText>(R.id.etFlats)
        var isTextFromUser = true

        binding.etAddress.setOnClickListener {
            if (!Places.isInitialized()) {
                Places.initialize(this, getString(R.string.google_maps_key))
            }
            val placesClient = Places.createClient(this)

            //   val rvSearchResults = bottomSheetView.findViewById<RecyclerView>(R.id.rvSearchResults)
            val searchAdapter = SearchSuggestionsAdapter { prediction ->
                val placeId = prediction.placeId
                val request = FetchPlaceRequest.builder(
                    placeId,
                    listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
                ).build()

                placesClient.fetchPlace(request).addOnSuccessListener { response ->
                    val place = response.place
                    finalAddress = place.address

                    val latLng = place.latLng
                    if (latLng != null) {
                        latitude = latLng.latitude
                        longitude = latLng.longitude

                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addressList =
                            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (!addressList.isNullOrEmpty()) {
                            val addr = addressList[0]
//                            finalAddress = addr.getAddressLine(0)

                            val featureName = addr.featureName ?: ""
                            val fullAddress = addr.getAddressLine(0) ?: ""

                            val cleanedAddress = if (fullAddress.startsWith(featureName)) {
                                fullAddress.removePrefix(featureName).trimStart(',', ' ')
                            } else {
                                fullAddress
                            }
                            val area = addr.subLocality
                            val city = addr.locality
                            val state = addr.adminArea
                            headingAddress = listOfNotNull(area, city, state).joinToString(", ")
                            finalAddress = cleanedAddress
                        } else {
                            headingAddress = place.name ?: place.address
                        }
                    }
                    /*isTextFromUser = false
                    etStreetInBottomSheet?.setText(headingAddress + "\n" + finalAddress)
                    etSearch?.setText(finalAddress)
                    etSearch?.clearFocus()
                    isTextFromUser = true
                    rvSearchResults.visibility = View.GONE*/

                }.addOnFailureListener {
                    Log.e("PlacesError", "Failed to fetch place details", it)
                    Toast.makeText(this, "Failed to fetch place details", Toast.LENGTH_SHORT).show()
                }
            }

            /*  rvSearchResults.layoutManager = LinearLayoutManager(this)
              rvSearchResults.adapter = searchAdapter
  */
            /*    etSearch.addTextChangedListener {
                    if (!isTextFromUser) return@addTextChangedListener
                    val query = it.toString()
                    if (query.length > 2) {
                        val token = AutocompleteSessionToken.newInstance()
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setSessionToken(token)
                            .setQuery(query)
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                searchAdapter.submitList(response.autocompletePredictions)
                                rvSearchResults.visibility = View.VISIBLE
                            }
                            .addOnFailureListener { e ->
                                Log.e("PlacesError", "Prediction failed", e)
                                Toast.makeText(
                                    this,
                                    "Prediction failed: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        rvSearchResults.visibility = View.GONE
                    }
                }

                etStreetInBottomSheet = bottomSheetView.findViewById(R.id.etStreetAddress)
    */
            bottomSheetView.findViewById<Button>(R.id.btnNearCurrent).setOnClickListener {
                //  getCurrentLocation()
                val intent = Intent(this, MapPickerActivity::class.java)
                mapResultLauncher.launch(intent)
                bottomSheetDialog.dismiss()

            }

            bottomSheetView.findViewById<Button>(R.id.btnFarAway).setOnClickListener {
                val intent = Intent(this, MapPickerActivity::class.java)
                mapResultLauncher.launch(intent)
                bottomSheetDialog.dismiss()

            }
            bottomSheetView.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            /*
                        bottomSheetView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                            // Validate flat number field first
                            if (etFlats.text.toString().isBlank()) {
                                Toast.makeText(this, "Flat / House No. is a required field", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            // Validate location
                            if (headingAddress.isNullOrBlank() || finalAddress.isNullOrBlank()) {
                                Toast.makeText(this, "Please select a location to proceed", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            // All good: show formatted address and dismiss
                            val finalText = "$headingAddress\n$finalAddress"
                            binding.etAddress.text = SpannableStringBuilder(finalText)
                            bottomSheetDialog.dismiss()
                        }
            */
            bottomSheetDialog.show()
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                latitude = location.latitude
                longitude = location.longitude

                val geocoder = Geocoder(this, Locale.getDefault())
                val address = geocoder.getFromLocation(lat, lng, 1)
                if (!address.isNullOrEmpty()) {
                    val addr = address[0]
//                    finalAddress = addr.getAddressLine(0)
                    val featureName = addr.featureName ?: ""
                    val fullAddress = addr.getAddressLine(0) ?: ""

                    val cleanedAddress = if (fullAddress.startsWith(featureName)) {
                        fullAddress.removePrefix(featureName).trimStart(',', ' ')
                    } else {
                        fullAddress
                    }
                    val area = addr.subLocality
                    val city = addr.locality
                    val state = addr.adminArea
                    headingAddress = "$area, $city, $state"
                    finalAddress = cleanedAddress
                }

                //    etStreetInBottomSheet?.setText(headingAddress + "\n" + finalAddress)
            } else {
                Toast.makeText(this, "Couldn't fetch location", Toast.LENGTH_SHORT).show()
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