package com.app.gdl.presentation.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.databinding.ActivityMapPickerBinding
import com.app.gdl.presentation.ui.adapters.SearchSuggestionsAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import java.util.Locale

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private var marker: Marker? = null
    private lateinit var binding: ActivityMapPickerBinding
    private var finalAddress : String? = null
    private var headingAddress : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        val placesClient = Places.createClient(this)
        geocoder = Geocoder(this, Locale.getDefault())

        val etSearch = findViewById<EditText>(R.id.etSearchLocation)
        val rvSearchResults = findViewById<RecyclerView>(R.id.rvSearchResults)

        val searchAdapter = SearchSuggestionsAdapter { prediction ->
            val placeId = prediction.placeId
            val request = FetchPlaceRequest.builder(
                placeId,
                listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS)
            ).build()

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    val latLng = place.latLng ?: return@addOnSuccessListener

                    marker?.remove()
                    marker = map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Selected Place")
                            .draggable(true)
                    )
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
                        val predictions = response.autocompletePredictions
                        searchAdapter.submitList(predictions)
                        rvSearchResults.visibility = View.VISIBLE
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Prediction failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                rvSearchResults.visibility = View.GONE
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnSelectLocation.setOnClickListener {
            val latLng = marker?.position
            if (latLng != null) {
                val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!address.isNullOrEmpty()) {
                    val address = address[0]
                    finalAddress = address.getAddressLine(0)
                    val area = address.subLocality
                    val city = address.locality
                    val state = address.adminArea
                    val postalCode = address.postalCode
                    val country = address.countryName
                    headingAddress = area + ", " + city + ", " + state
                }
                val resultIntent = Intent().apply {
                    putExtra("lat", latLng.latitude)
                    putExtra("lng", latLng.longitude)
                    putExtra("address", finalAddress)
                    putExtra("headingAddress", headingAddress)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

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

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val defaultLatLng = if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                LatLng(28.6139, 77.2090)
            }

            marker = map.addMarker(
                MarkerOptions()
                    .position(defaultLatLng)
                    .title("Selected Location")
                    .draggable(true)
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 18f))
        }

        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDrag(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                this@MapPickerActivity.marker = marker
            }
        })
    }
}
