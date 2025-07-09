package com.app.gdl.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.R
import com.app.gdl.data.model.PriceItem
import com.app.gdl.data.model.User
import com.app.gdl.databinding.FragmentHomeBinding
import com.app.gdl.presentation.ui.adapters.FeatureAdapter
import com.app.gdl.presentation.ui.adapters.PopularCategoryAdapter
import com.app.gdl.presentation.ui.adapters.PopularItemsAdapter
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.ui.adapters.ShopByCategoryAdapter
import com.app.gdl.presentation.viewmodel.CategoryViewModel
import com.app.gdl.presentation.viewmodel.DefaultPriceViewModel
import com.app.gdl.presentation.viewmodel.PopularItemViewModel
import com.app.gdl.presentation.viewmodel.PopularViewModel
import com.app.gdl.presentation.viewmodel.ProductViewModel
import com.app.gdl.presentation.viewmodel.ShopCategoryViewModel
import com.app.gdl.utils.SharedPref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), FeatureAdapter.OnProductClickListener,
    ProductAdapter.OnProductListener,
    ProductAdapter.AddToCartListener {
    override fun onProductClicked(categoryId: String, categoryName: String) {
        val fragment = ProductByCategoryFragment.newInstance(categoryId, categoryName)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }

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
    var priceclass = ""
    private var customer: User? = null
    lateinit var prefs: SharedPref
    private val defaultPriceViewModel: DefaultPriceViewModel by viewModels()
    /* private var etStreetInBottomSheet: EditText? = null
     private var etSearch: EditText? = null
     private var finalAddress: String? = null
     private var headingAddress: String? = null
     var addressUser: String = ""
     lateinit var prefs: SharedPref
    private val mapResultLauncher =
         registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
             if (it.resultCode == Activity.RESULT_OK) {
                 val address = it.data?.getStringExtra("address") ?: ""
                 headingAddress = it.data?.getStringExtra("headingAddress")
                 etStreetInBottomSheet?.setText(finalAddress)
                 etSearch?.setText(finalAddress)

             }
         }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SharedPref(requireContext())
        // addressUser = arguments?.getString("addressUser").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
        // setupPickLocation()

    }

    private fun setupUi() {
        featureAdapter = FeatureAdapter(this)

        val gridLayoutManager = GridLayoutManager(context, 2)
        binding.featureRecycleview.layoutManager = gridLayoutManager
        binding.featureRecycleview.adapter = featureAdapter

        shopByCategoryAdapter = ShopByCategoryAdapter(this)
        binding.categoryRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.categoryRecyclerView.adapter = shopByCategoryAdapter

        popularAdapter = PopularCategoryAdapter(
            lifecycleOwner = this,
            productViewModel = productViewModel,
            listener = this,
            productlistener = this,
            addToCartListener = this
        )
        binding.productsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.productsRecyclerView.adapter = popularAdapter

        popularItemsAdapter = PopularItemsAdapter(this, this)
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
            popularAdapter.submitList(response.category_list, requireContext())
        }

        popularItemViewModel.products.observe(viewLifecycleOwner) { response ->
            popularItemsAdapter.submitData(response.list, response.s3_img_path)
        }


        categoryViewModel.fetchCategories()
        shopCategoryViewModel.fetchAllCategories()
        popularViewModel.fetchGetpopularCategories()
        popularItemViewModel.fetchPopularItems()


        // call the default price api with priceclass for login user MLD CBD
        customer = prefs.getCustomerFromPrefs(requireContext())
        priceclass = customer?.address?.get(0)?.price_class.toString()
        Log.d("priceclass", "onCreate: " + priceclass)

        /*
                if (priceclass.isNotEmpty() && prefs.isLoggedIn) {
                    Log.d("ResponseWith Price", priceclass.isNotEmpty().toString()+""+prefs.isLoggedIn)
                    val inventoryIds = prefs.getInventoryIds(requireContext())
                    Log.d("SIZE IN PREF IDS", inventoryIds.size.toString())

                    defaultPriceViewModel.defaultPrice.observe(viewLifecycleOwner) { response ->
                        Log.d("ResponseWith Price", "Received: $response")

                        // Group by CustomerPriceClass
                        val groupedByPrice = response.list.groupBy { it.CustomerPriceClass.value }
                        Log.d("GroupedByPrice", groupedByPrice.toString())

                        // Get items for this price class
                        val priceItems = groupedByPrice[priceclass] ?: emptyList()
                        Log.d("PriceItems", priceItems.toString())

                        // Filter using Inventory IDs
                        val filteredPriceItems = priceItems.filter { inventoryIds.contains(it.InventoryID.value) }
                        Log.d("FilteredItems", filteredPriceItems.toString())

                    }

                    defaultPriceViewModel.fetchDefaultPrice(priceclass)
                }
        */
        if (priceclass.isNotEmpty() && prefs.isLoggedIn) {
            Log.d("Check", "PriceClass: $priceclass, LoggedIn: ${prefs.isLoggedIn}")

            val inventoryIds = prefs.getInventoryIds(requireContext())
            Log.d("SIZE IN PREF IDS", inventoryIds.size.toString())

            // Call this BEFORE observe
            defaultPriceViewModel.fetchDefaultPrice(priceclass)

            defaultPriceViewModel.defaultPrice.observe(viewLifecycleOwner) { response ->
                Log.d("ResponseWith Price", "Received: $response")

                val groupedByPrice = response.list.groupBy { it.CustomerPriceClass.value }
                Log.d("groupedByPrice", "Received: $groupedByPrice")

                val priceItems = groupedByPrice[priceclass] ?: emptyList()
                Log.d("priceItems", "Received: $priceItems")

                val filteredPriceItems =
                    priceItems.filter { inventoryIds.contains(it.InventoryID.value) }

                Log.d("FilteredItems", filteredPriceItems.toString())
                popularItemsAdapter.setPriceMap(filteredPriceItems)

            }
        }/* else {
            defaultPriceViewModel.defaultPrice.observe(viewLifecycleOwner) { response ->
                Log.d("ResponseWithoutLogin", "Received: $response")

                val groupedByPrice = response.list.groupBy { it.CustomerPriceClass.value }
                val priceItems = groupedByPrice[priceclass] ?: emptyList()
                Log.d("PriceItemsNoLogin", priceItems.toString())

            }

            defaultPriceViewModel.fetchDefaultPrice(priceclass)
        }*/

    }

    /*
        private fun setupPickLocation() {
            var isTextFromUser = true
            val bottomSheetView = layoutInflater.inflate(R.layout.layout_location_bottom_sheet, null)
            etSearch = bottomSheetView.findViewById(R.id.etSearchLocation)
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(bottomSheetView)
            if (addressUser != null) {
                binding.deliveryLocation.text = prefs.userAdrress
            } else {
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
                        etStreetInBottomSheet?.setText(finalAddress)
                        etSearch?.setText(finalAddress)
                        etSearch?.clearFocus()
                        isTextFromUser = true
                        rvSearchResults.visibility = View.GONE


                    }.addOnFailureListener {
                        Log.e("PlacesError", "Failed to fetch place details", it)
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch place details",
                            Toast.LENGTH_SHORT
                        ).show()
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
                                Toast.makeText(
                                    requireContext(),
                                    "Prediction failed: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                    binding.deliveryLocation.text =
                        "Deliver to: ${SpannableStringBuilder(headingAddress)}"
                    bottomSheetDialog.dismiss()
                }

                bottomSheetDialog.show()
            }
        }
    */

    /*
        private fun getCurrentLocation() {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    101
                )
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

                    etStreetInBottomSheet?.setText(finalAddress)
                    etSearch?.setText(finalAddress)

                } else {
                    Toast.makeText(requireContext(), "Couldn't fetch location", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    */

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onProductDataClicked(inventoryId: String) {
        val fragment = ProductByCategoryDetailsFragment.newInstance(inventoryId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun addToCartClicked() {
        val fragment = ShoppingCartFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }

}
