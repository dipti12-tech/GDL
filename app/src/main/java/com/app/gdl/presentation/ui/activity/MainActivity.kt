package com.app.gdl.presentation.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.data.model.Warehouse
import com.app.gdl.databinding.ActivityMainBinding
import com.app.gdl.databinding.ToolbarHeaderBinding
import com.app.gdl.domain.repository.WarehouseRepository
import com.app.gdl.presentation.ui.adapters.CartAdapter
import com.app.gdl.presentation.ui.adapters.SearchSuggestionsAdapter
import com.app.gdl.presentation.ui.fragment.HomeFragment
import com.app.gdl.presentation.ui.fragment.ShoppingCartFragment
import com.app.gdl.presentation.viewmodel.CategoryViewModel
import com.app.gdl.presentation.viewmodel.WarehouseViewModel
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.math.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),CartAdapter.CartItemCountListener {

   // private var etStreetInBottomSheet: EditText? = null
   // private var etSearch: EditText? = null
    private var finalAddress: String? = null
    private var headingAddress: String? = null
   // var addressUser: String = ""
    var from: String=""
    lateinit var prefs: SharedPref
    private val warehouseViewModel: WarehouseViewModel by viewModels()

    private val mapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val address = it.data?.getStringExtra("address")
                headingAddress = it.data?.getStringExtra("headingAddress")
                finalAddress = address
                binding.deliveryLocation.text = finalAddress
               // addressType = it.data?.getStringExtra("addressType").toString()
              //  lat = it.data?.getDoubleExtra("lat", 0.0) ?: 0.0
               // lng = it.data?.getDoubleExtra("lng", 0.0) ?: 0.0

            }
        }
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarBinding: ToolbarHeaderBinding

    var address: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = SharedPref(this)

        setupPickLocation()
        getCurrentLocation()
    //    addressUser = intent.getStringExtra("addressUser").toString()
        from = intent.getStringExtra("from").toString()

        if(from.equals("SignUp")){
            CartManager.clearCart()
        }
        val headerView = binding.navView.getHeaderView(0)
       val tvName = headerView.findViewById<TextView>(R.id.textViewName)
        val tvMobile = headerView.findViewById<TextView>(R.id.textViewMobile)
        Log.d("TAG", "onCreate: " +"Name"+ prefs.name+"Mobile"+prefs.mobile+"Address"+prefs.userAdrress)

        tvName.text = if (!prefs.name.isNullOrBlank()) prefs.name else "Guest User"
        tvMobile.text = prefs.mobile.orEmpty()


        // Set Toolbar
        toolbarBinding = binding.toolbarHeader
        setSupportActionBar(toolbarBinding.customToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        Log.d("CartManagerMainactivity", "Cart size: ${CartManager.getItems().size}")
        if(CartManager.getItems().size==0){
            toolbarBinding.cartBadge.visibility=View.GONE
        }else{
            toolbarBinding.cartBadge.visibility=View.VISIBLE
            toolbarBinding.cartBadge.text = "${CartManager.getItems().size}"
        }

        toolbarBinding.menuIcon.load("file:///android_asset/hamberger.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        toolbarBinding.userIcon.load("file:///android_asset/user.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        toolbarBinding.cartIcon.load("file:///android_asset/shoppingcart.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        // Menu icon opens/closes drawer
        toolbarBinding.menuIcon.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        // User Icon click listener (if needed)
        toolbarBinding.userIcon.setOnClickListener {
            // You can navigate to user profile or show a dialog here
        }

        // Cart Icon click listener (if needed)
        toolbarBinding.cartIcon.setOnClickListener {
            // Navigate to cart screen or fragment
            navigateTo(ShoppingCartFragment.newInstance())

        }
        toolbarBinding.cartBadge.setOnClickListener {
            // Navigate to cart screen or fragment
            navigateTo(ShoppingCartFragment.newInstance())

        }



        // Set up NavigationView menu item selection
        binding.navView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navigateTo(HomeFragment.newInstance(address))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_logout -> {
                    prefs.isLoggedIn = false
                    prefs.clearSession()
                 //   CartManager.clearCart()
                    intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)

                    true
                }

                else -> false
            }
        }

        if (savedInstanceState == null) {
            navigateTo(HomeFragment.newInstance(address))
        }
        warehouseViewModel.loadWarehouses()
        warehouseViewModel.warehouses.observe(this) { response  ->
            Log.d("ResponseWith Warehouse", "Received: $response")
            val cities = response.map { it.city }.distinct()
            Log.d("ResponseWith Warehouse", "Received: ${cities.toString()}")

        }
    }

    override fun onBackPressed() {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()  // Pops the current fragment
            } else {
                super.onBackPressed()
        }

    }
    private fun navigateTo(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.simpleName)
        }

        transaction.commit()
    }

    private fun setupPickLocation() {
        var isTextFromUser = true
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_location_bottom_sheet, null)
     //   etSearch = bottomSheetView.findViewById(R.id.etSearchLocation)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)
        if (prefs.userAdrress?.isNotEmpty() == true) {

            binding.deliveryLocation.text = prefs.userAdrress
        }
        binding.deliveryLocation.setOnClickListener {
            if (!Places.isInitialized()) {
                Places.initialize(this, getString(R.string.google_maps_key))
            }
            val placesClient = Places.createClient(this)

          //  val rvSearchResults = bottomSheetView.findViewById<RecyclerView>(R.id.rvSearchResults)
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

                    isTextFromUser = false
                   /* etStreetInBottomSheet?.setText(finalAddress)
                    etSearch?.setText(finalAddress)
                    etSearch?.clearFocus()
                    isTextFromUser = true
                    rvSearchResults.visibility = View.GONE*/


                }.addOnFailureListener {
                    Log.e("PlacesError", "Failed to fetch place details", it)
                    Toast.makeText(
                        this,
                        "Failed to fetch place details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

         /*   rvSearchResults.layoutManager = LinearLayoutManager(this)
            rvSearchResults.adapter = searchAdapter
*/
         /*   val etSearch = bottomSheetView.findViewById<EditText>(R.id.etSearchLocation)
            etSearch.addTextChangedListener {
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
*/
          //  etStreetInBottomSheet = bottomSheetView.findViewById(R.id.etStreetAddress)

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

                val geocoder = Geocoder(this, Locale.getDefault())
                val address = geocoder.getFromLocation(lat, lng, 1)
                if (!address.isNullOrEmpty()) {
                    val addr = address[0]
//                  finalAddress = addr.getAddressLine(0)
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
                binding.deliveryLocation.text =
                    "${SpannableStringBuilder(finalAddress)}"

          //      etStreetInBottomSheet?.setText(finalAddress)
          //      etSearch?.setText(finalAddress)

            } else {
                Toast.makeText(this, "Couldn't fetch location", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCartItemCountChanged(count: Int) {
        Log.d("COUNT", "onCartItemCountChanged: "+count)
        if(count==0){
            toolbarBinding.cartBadge.visibility =View.GONE
        }else
        {
            toolbarBinding.cartBadge.visibility =View.VISIBLE
            toolbarBinding.cartBadge.text ="${count}"
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getCurrentLocation()
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied. Please allow it in settings.", Toast.LENGTH_LONG).show()
                //show the cities list popup here
            }
        }
    }

}
