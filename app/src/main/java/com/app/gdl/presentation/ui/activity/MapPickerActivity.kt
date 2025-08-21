package com.app.gdl.presentation.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.R
import com.app.gdl.databinding.ActivityMapPickerBinding
import com.app.gdl.databinding.AddressInputBottomSheetBinding
import com.app.gdl.presentation.ui.adapters.SearchSuggestionsAdapter
import com.app.gdl.utils.SharedPref
import com.app.gdl.utils.ToastMessage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import java.util.Locale

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var binding: ActivityMapPickerBinding
    private var finalAddress: String? = null
    private var headingAddress: String? = null
    private var cityin: String? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBinding: AddressInputBottomSheetBinding
    private val saveasAddress = listOf("Home", "Work", "Other")
    private var addressType: String = ""
    var lat: Double = 0.0
    var lng: Double = 0.0
    lateinit var prefs: SharedPref

    private val mapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val address = it.data?.getStringExtra("address")
                headingAddress = it.data?.getStringExtra("headingAddress")
                finalAddress = address
                addressType = it.data?.getStringExtra("addressType").toString()
                lat = it.data?.getDoubleExtra("lat", 0.0) ?: 0.0
                lng = it.data?.getDoubleExtra("lng", 0.0) ?: 0.0
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = SharedPref(this)
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        val placesClient = Places.createClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        val etSearch = binding.etSearchLocation
        val rvSearchResults = binding.rvSearchResults

        val searchAdapter = SearchSuggestionsAdapter { prediction ->
            val placeId = prediction.placeId
            val request = FetchPlaceRequest.builder(
                placeId,
                listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
            ).build()

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val latLng = response.place.latLng ?: return@addOnSuccessListener
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    rvSearchResults.visibility = View.GONE
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch place", Toast.LENGTH_SHORT).show()
                }
        }

        rvSearchResults.layoutManager = LinearLayoutManager(this)
        rvSearchResults.adapter = searchAdapter

        etSearch.addTextChangedListener {
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
                        Toast.makeText(this, "Prediction failed: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                rvSearchResults.visibility = View.GONE
            }
        }
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnSelectLocation.setOnClickListener {

            showAddressBottomSheet()
        }

        binding.btnUseCurrentLocation.setOnClickListener {
            getLastLocationAndMoveCamera()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        map.isMyLocationEnabled = true
        getLastLocationAndMoveCamera()

        // Fetch address when camera stops moving
        map.setOnCameraIdleListener {
            val target = map.cameraPosition.target
            val addressList = geocoder.getFromLocation(target.latitude, target.longitude, 1)

            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val building =
                    address.premises ?: address.featureName ?: address.subThoroughfare ?: ""
                val area = address.subLocality ?: ""
                val city = address.locality ?: ""
                val state = address.adminArea ?: ""
                cityin = city
                Log.d("City in Map Picker", "onMapReady: " + city)
                finalAddress = listOf(building, area, city, state)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                headingAddress = listOf(building, area)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                binding.tvHeadingAddress.text = headingAddress
                binding.tvFinalAddress.text = finalAddress
            }
        }
    }

    private fun getLastLocationAndMoveCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val defaultLatLng = location?.let {
                LatLng(it.latitude, it.longitude)
            } ?: LatLng(28.6139, 77.2090) // fallback to Delhi

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 18f))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocationAndMoveCamera()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateDistance(target: LatLng): Double {
        val lastKnown = map.myLocation
        return if (lastKnown != null) {
            val start = Location("").apply {
                latitude = lastKnown.latitude
                longitude = lastKnown.longitude
            }
            val end = Location("").apply {
                latitude = target.latitude
                longitude = target.longitude
            }
            start.distanceTo(end).toDouble() / 1000
        } else 0.0
    }

    private fun showAddressBottomSheet() {
        bottomSheetBinding = AddressInputBottomSheetBinding.inflate(LayoutInflater.from(this))
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.tvLocality.text = headingAddress
        setupAddressChips(saveasAddress)

        bottomSheetBinding.btnSaveAddress.setOnClickListener {

            val name = bottomSheetBinding.etName.text.toString().trim()
            val building = bottomSheetBinding.etBuilding.text.toString().trim()
            val floor = bottomSheetBinding.etFloor.text.toString().trim()
            val landmark = bottomSheetBinding.etLandmark.text.toString().trim() // optional
            val locality = bottomSheetBinding.tvLocality.text.toString().trim()
            val isCustom = addressType.lowercase() == "other"

            if (isCustom) {
                bottomSheetBinding.etName.visibility = View.VISIBLE
                bottomSheetBinding.txtPhone.visibility = View.VISIBLE
                val customerData = prefs.getCustomerDetailsFromPrefs(this)
                bottomSheetBinding.txtPhone.text = customerData?.phone
            } else {
                bottomSheetBinding.etName.visibility = View.GONE
                bottomSheetBinding.txtPhone.visibility = View.GONE
            }

            if (isCustom && name.isEmpty()) {
                bottomSheetBinding.etName.error = "Name is required for 'Other' address"
                return@setOnClickListener
            }

            if (addressType.isEmpty()) {
                ToastMessage(this, "Select the Type of Address")
                return@setOnClickListener
            }
            if (building.isEmpty()) {
                bottomSheetBinding.etBuilding.error = "This is a required field"
                return@setOnClickListener
            }

            if (floor.isEmpty()) {
                bottomSheetBinding.etFloor.error = "This is a required field"
                return@setOnClickListener
            }
            if (landmark.isEmpty()) {
                bottomSheetBinding.etLandmark.error = "This is a required field"
                return@setOnClickListener
            }

            if (locality.isEmpty()) {
                Toast.makeText(this, "Please select locality", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val addressParts = listOf(building, floor, landmark, locality)
            val finalAddress = addressParts.filter { it.isNotBlank() }.joinToString(", ")

            val resultIntent = Intent().apply {
                putExtra("lat", map.cameraPosition.target.latitude)
                putExtra("lng", map.cameraPosition.target.longitude)
                putExtra("address", "$landmark, $locality")
                putExtra("headingAddress", finalAddress)
                putExtra("addressType", addressType)
                putExtra("city", cityin)
            }
            prefs.selectedAddress = locality

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
            bottomSheetDialog.dismiss()
        }

        // Handle Change Locality
        bottomSheetBinding.btnChangeLocation.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            mapResultLauncher.launch(intent)
            bottomSheetDialog.dismiss()

        }

        bottomSheetDialog.show()
    }


    private fun setupAddressChips(saveasAddress: List<String>) {
        bottomSheetBinding.chipGroupAddressType.removeAllViews()

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
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue))
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

            bottomSheetBinding.chipGroupAddressType.addView(chip)
        }


        bottomSheetBinding.chipGroupAddressType.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            chip?.let {
                addressType = "${chip.text}"
                val isCustom =
                    addressType.lowercase() != "home" && addressType.lowercase() != "work"

                bottomSheetBinding.etName.visibility = View.VISIBLE
                bottomSheetBinding.txtPhone.visibility = View.VISIBLE
                val customerData = prefs.getCustomerDetailsFromPrefs(this)
                bottomSheetBinding.txtPhone.text = customerData?.phone
                if (!isCustom) {
                    bottomSheetBinding.etName.visibility = View.GONE
                    bottomSheetBinding.txtPhone.visibility = View.GONE
                }
            }
        }
    }
}
