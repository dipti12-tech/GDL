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
import com.app.gdl.data.model.Address
import com.app.gdl.databinding.ActivityMapPickerBinding
import com.app.gdl.databinding.AddressInputBottomSheetBinding
import com.app.gdl.presentation.ui.adapters.SearchSuggestionsAdapter
import com.app.gdl.utils.AuthPromptDialog
import com.app.gdl.utils.SharedPref
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

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetBinding: AddressInputBottomSheetBinding
    private val saveasAddress = listOf("Home", "Work", "Other")
    private var addressType: String = ""
    var lat:Double =0.0
    var lng:Double =0.0
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
                Log.d(
                    "NewAddressDetails",
                    "Signup Request: $finalAddress addressType $addressType lat $lat lng  $lng")
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

        // Setup adapter for predictions
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
                        Toast.makeText(this, "Prediction failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                rvSearchResults.visibility = View.GONE
            }
        }

        // Setup map
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Confirm Button Click
     /*   binding.btnSelectLocation.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("lat", map.cameraPosition.target.latitude)
                putExtra("lng", map.cameraPosition.target.longitude)
                putExtra("address", finalAddress)
                putExtra("headingAddress", headingAddress)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
*/

        binding.btnSelectLocation.setOnClickListener {

            showAddressBottomSheet()
        }

        // Use Current Location Button Click
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
                val building = address.premises ?: address.featureName ?: address.subThoroughfare ?: ""
                val area = address.subLocality ?: ""
                val city = address.locality ?: ""
                val state = address.adminArea ?: ""

                finalAddress = listOf(building, area, city, state)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                headingAddress = listOf(building, area)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                // Update bottom card UI
                binding.tvHeadingAddress.text = headingAddress
                binding.tvFinalAddress.text = finalAddress
              /*  binding.tvDistance.text =
                    "Pin location is approx. %.2f km away from your current location".format(
                        calculateDistance(target)
                    )*/
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

        // Handle Close
        bottomSheetBinding.btnClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetBinding.tvLocality.text = headingAddress
        setupAddressChips(saveasAddress)
        /*   val centerLatLng = map.cameraPosition.target

                      lifecycleScope.launch {
                          try {
                              val addressList = withContext(Dispatchers.IO) {
                                  geocoder.getFromLocation(centerLatLng.latitude, centerLatLng.longitude, 1)
                              }

                              if (!addressList.isNullOrEmpty()) {
                                  val address = addressList[0]

                                  val building = address.premises ?: address.featureName ?: address.subThoroughfare ?: ""
                                  val area = address.subLocality ?: ""
                                  val city = address.locality ?: ""
                                  val state = address.adminArea ?: ""

                                  finalAddress = listOf(building, area, city, state)
                                      .filter { it.isNotBlank() }
                                      .joinToString(", ")

                                  headingAddress = listOf(building, area, city, state)
                                      .filter { it.isNotBlank() }
                                      .joinToString(", ")

                                  Log.d("headingAddress$$$", "headingAddress: $headingAddress")
                                  Log.d("finalAddress$$$", "finalAddress: $finalAddress")

                                  val resultIntent = Intent().apply {
                                      putExtra("lat", centerLatLng.latitude)
                                      putExtra("lng", centerLatLng.longitude)
                                      putExtra("address", finalAddress)
                                      putExtra("headingAddress", headingAddress)
                                      putExtra("addressType", addressType)
                                  }
                                  setResult(Activity.RESULT_OK, resultIntent)
                                  finish()
                              } else {
                                  Toast.makeText(this@MapPickerActivity, "Unable to get address", Toast.LENGTH_SHORT).show()
                              }

                          } catch (e: Exception) {
                              e.printStackTrace()
                              Toast.makeText(this@MapPickerActivity, "Failed to fetch address", Toast.LENGTH_SHORT).show()
                          }
                      }*/
        // Handle Save Address
        bottomSheetBinding.btnSaveAddress.setOnClickListener {

            if(prefs.isLoggedIn){
            val building = bottomSheetBinding.etBuilding.text.toString().trim()
            val floor = bottomSheetBinding.etFloor.text.toString().trim()
            val landmark = bottomSheetBinding.etLandmark.text.toString().trim()
            val locality = bottomSheetBinding.tvLocality.text.toString().trim()


            val resultIntent = Intent().apply {
                putExtra("lat", map.cameraPosition.target.latitude)
                putExtra("lng", map.cameraPosition.target.longitude)
                //putExtra("address", building+","+floor+","+landmark+","+locality)
                // putExtra("headingAddress", building+","+floor+","+landmark+","+locality)
                val addressParts = listOf(building, floor, landmark, locality)
                val finalAddress = addressParts.filter { it.isNotBlank() }.joinToString(", ")

                intent.putExtra("address", finalAddress)
                intent.putExtra("headingAddress", finalAddress)

                putExtra("addressType", addressType)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

            //  Toast.makeText(this, "Saved for: $orderingFor, as $addressType", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }else{
                AuthPromptDialog(
                    activity = this,
                    onRegisterClicked = {
                        // Navigate to Register screen
                        startActivity(Intent(this, SignUpActivity::class.java))
                    },
                    onSignInClicked = {
                        // Navigate to Login screen
                        startActivity(Intent(this, SignInActivity::class.java))
                    }
                ).show()

            }
        }

        // Handle Change Locality
        bottomSheetBinding.btnChangeLocation.setOnClickListener {
            //Toast.makeText(this, "Change Location clicked", Toast.LENGTH_SHORT).show()
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
                //   Toast.makeText(this, "Selected Type: ${chip.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
