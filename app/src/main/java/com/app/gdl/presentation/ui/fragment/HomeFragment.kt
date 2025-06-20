package com.app.gdl.presentation.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.databinding.FragmentHomeBinding
import com.app.gdl.presentation.ui.activity.MapPickerActivity
import com.app.gdl.presentation.ui.adapters.*
import com.app.gdl.presentation.viewmodel.*
import com.app.gdl.utils.SharedPref
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {
    companion object {
        private const val ADDRESS = "addressUser"

        fun newInstance(address: String): HomeFragment {
            val fragment = HomeFragment()
            val bundle = Bundle().apply {
                putString(ADDRESS, address)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
    private lateinit var binding: FragmentHomeBinding

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val shopCategoryViewModel: ShopCategoryViewModel by viewModels()
    private val popularViewModel: PopularViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val popularItemViewModel: PopularItemViewModel by viewModels()


    private lateinit var featureAdapter: FeatureAdapter
    private lateinit var shopByCategoryAdapter: ShopByCategoryAdapter
    private lateinit var popularAdapter: PopularCategoryAdapter
    private lateinit var popularItemsAdapter: PopularItemsAdapter

    private var etStreetInBottomSheet: EditText? = null
    private var etSearch: EditText? = null
    private var finalAddress: String? = null
    private var headingAddress: String? = null
    var addressUser:String =""
    lateinit var  prefs :SharedPref
    private val mapResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val address = it.data?.getStringExtra("address") ?: ""
            headingAddress = it.data?.getStringExtra("headingAddress")
            etStreetInBottomSheet?.setText(finalAddress)
            etSearch?.setText(finalAddress)

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SharedPref(requireContext())
        addressUser = arguments?.getString("addressUser").toString()

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
        setupPickLocation()
    }

    private fun setupUi() {
        featureAdapter = FeatureAdapter()
        binding.featureRecycleview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.featureRecycleview.adapter = featureAdapter

        shopByCategoryAdapter = ShopByCategoryAdapter()
        binding.categoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.categoryRecyclerView.adapter = shopByCategoryAdapter

        popularAdapter = PopularCategoryAdapter(
            lifecycleOwner = this,
            productViewModel = productViewModel
        )
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.productsRecyclerView.adapter = popularAdapter

        popularItemsAdapter = PopularItemsAdapter()
        binding.PastandPopularRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.PastandPopularRecyclerView.adapter = popularItemsAdapter

    }

    private fun setupObservers() {
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            featureAdapter.submitList(it.category_list)
        }

        shopCategoryViewModel.allcategories.observe(viewLifecycleOwner) {
            shopByCategoryAdapter.submitList(it.category_list)
        }

        popularViewModel.getpopularcategories.observe(viewLifecycleOwner) { response ->
            popularAdapter.submitList(response.category_list,requireContext())
        }

        popularItemViewModel.products.observe(viewLifecycleOwner) { response ->
            popularItemsAdapter.submitData(response.list,response.s3_img_path)
        }

        categoryViewModel.fetchCategories()
        shopCategoryViewModel.fetchAllCategories()
        popularViewModel.fetchGetpopularCategories()
        popularItemViewModel.fetchPopularItems()
    }

    private fun setupPickLocation() {
        var isTextFromUser = true
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_location_bottom_sheet, null)
        etSearch = bottomSheetView.findViewById(R.id.etSearchLocation)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)
        if(addressUser!=null) {
            binding.deliveryLocation.text =  prefs.userAdrress
        }else{
            binding.deliveryLocation.text = "Select Address"

        }
        binding.deliveryLocation.setOnClickListener {
            if (!Places.isInitialized()) {
                Places.initialize(requireContext(), getString(R.string.google_maps_key))
            }
            val placesClient = Places.createClient(requireContext())

            val rvSearchResults = bottomSheetView.findViewById<RecyclerView>(R.id.rvSearchResults)
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
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
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
                    etStreetInBottomSheet?.setText(finalAddress)
                    etSearch?.setText(finalAddress)
                    etSearch?.clearFocus()
                    isTextFromUser = true
                    rvSearchResults.visibility = View.GONE


                }.addOnFailureListener {
                    Log.e("PlacesError", "Failed to fetch place details", it)
                    Toast.makeText(requireContext(), "Failed to fetch place details", Toast.LENGTH_SHORT).show()
                }
            }

            rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
            rvSearchResults.adapter = searchAdapter

            val etSearch = bottomSheetView.findViewById<EditText>(R.id.etSearchLocation)
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
                            Toast.makeText(requireContext(), "Prediction failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    rvSearchResults.visibility = View.GONE
                }
            }

            etStreetInBottomSheet = bottomSheetView.findViewById(R.id.etStreetAddress)

            bottomSheetView.findViewById<Button>(R.id.btnUseCurrentLocation).setOnClickListener {
                getCurrentLocation()
            }

            bottomSheetView.findViewById<Button>(R.id.btnOpenMap).setOnClickListener {
                val intent = Intent(requireContext(), MapPickerActivity::class.java)
                mapResultLauncher.launch(intent)
            }

            bottomSheetView.findViewById<Button>(R.id.btnSave).setOnClickListener {
                binding.deliveryLocation.text = "Deliver to: ${SpannableStringBuilder(headingAddress)}"
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude

                val geocoder = Geocoder(requireContext(), Locale.getDefault())
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

                etStreetInBottomSheet?.setText(finalAddress)
                etSearch?.setText(finalAddress)

            } else {
                Toast.makeText(requireContext(), "Couldn't fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
