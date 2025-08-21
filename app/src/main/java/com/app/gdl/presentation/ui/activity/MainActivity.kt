package com.app.gdl.presentation.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.BuildConfig
import com.app.gdl.R
import com.app.gdl.data.model.Warehouse
import com.app.gdl.databinding.ActivityMainBinding
import com.app.gdl.databinding.ToolbarHeaderBinding
import com.app.gdl.presentation.ui.adapters.CartAdapter
import com.app.gdl.presentation.ui.dialog.CityPickerDialogFragment
import com.app.gdl.presentation.ui.fragment.HomeFragment
import com.app.gdl.presentation.ui.fragment.MyOrdersHistoryFragment
import com.app.gdl.presentation.ui.fragment.ShopByCategoryFragment
import com.app.gdl.presentation.ui.fragment.ShoppingCartFragment
import com.app.gdl.presentation.viewmodel.WarehouseViewModel
import com.app.gdl.utils.AnalyticsHelper
import com.app.gdl.utils.AuthPromptDialog
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.PermissionManager
import com.app.gdl.utils.SharedPref
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CartAdapter.CartItemCountListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarBinding: ToolbarHeaderBinding

    private val warehouseViewModel: WarehouseViewModel by viewModels()
    private lateinit var prefs: SharedPref

    private var cities: List<String> = emptyList()
    private var warehousesList: List<Warehouse> = emptyList()

    private var finalAddress: String? = null
    private var headingAddress: String? = null
    private var address: String = ""
    private var warehouseData :String?=null
    private var from: String = ""
    private var addressFromSignUp: String = ""

    private val mapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val address = it.data?.getStringExtra("address")
                headingAddress = it.data?.getStringExtra("headingAddress")
                finalAddress = address
                binding.deliveryLocation.text = address
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AnalyticsHelper.logScreenView(this, "MainActivity")

        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbarNavigation()

        }

        prefs = SharedPref(this)
        from = intent.getStringExtra("from").orEmpty()
        addressFromSignUp = intent.getStringExtra("addressUser").orEmpty()

        if (from == "SignUp") CartManager.clearCart()

        if (!addressFromSignUp.isNullOrBlank() && addressFromSignUp != "null") {
            binding.deliveryLocation.text = SpannableStringBuilder(addressFromSignUp)
        } else {
            binding.deliveryLocation.text = prefs.userAdrress
        }

        observeWarehouseData()
        observeSelectedPriceClass()
        observeSelectedWarehouse()

        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()

        if (savedInstanceState == null) {
            navigateTo(HomeFragment.newInstance(address), addToBackStack = false)
        }
    }

    private fun observeWarehouseData() {
        warehouseViewModel.loadWarehouses()
        warehouseViewModel.warehouses.observe(this) { response ->
            if (!response.isNullOrEmpty()) {
                warehousesList = response
                cities = response.map { it.city }.distinct().toMutableList().apply {
                    add(0, "Select a city")
                }
                prefs.savedCities = cities

                if (!PermissionManager.hasAllPermissions(this)) {
                    if (!prefs.hasPermissionBeenRequestedOnce) {
                        prefs.hasPermissionBeenRequestedOnce = true
                        PermissionManager.requestAllPermissions(this)
                    } else if (!prefs.citySelected && from!="SignIn") {
                        showCityPopupIfNeeded()
                    }
                }
                else if (from == "SignUp") {
                    getCurrentCityOnly()
                }


                else if (!prefs.citySelected && from != "SignUp" && from != "SignIn" && from != "SignUp") {
                    showCityPopupIfNeeded()
                }
            }
        }
    }

    private fun observeSelectedPriceClass() {
        warehouseViewModel.selectedPriceClass.observe(this) { priceClass ->
            Handler(Looper.getMainLooper()).postDelayed({
                val fragment = supportFragmentManager.findFragmentById(R.id.main_content)
                if (fragment is HomeFragment) {
                    val customer = prefs.getCustomerFromPrefs(this)
                    val priceclass = customer?.address?.getOrNull(0)?.price_class.orEmpty()

                    if (prefs.isLoggedIn && priceclass != "null" && priceclass.isNotBlank()) {
                        fragment.fetchPopularItemsWithNewPriceClass(priceclass,warehouseData)
                    } else {
                        fragment.fetchPopularItemsWithNewPriceClass(priceClass,warehouseData)
                    }
                } else {
                    Log.e("MainActivity", "HomeFragment not attached yet")
                }
            }, 300)
        }
    }

    private fun observeSelectedWarehouse() {
        warehouseViewModel.selectedWarehouse.observe(this) { warehouse ->
            Log.d("SelectedWarehouse", "Warehouse: $warehouse")
            warehouseData= warehouse
        }
    }

    private fun setupToolbar() {
        toolbarBinding = binding.toolbarHeader
        setSupportActionBar(toolbarBinding.customToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarBinding.customToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

       // supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow) // Add your own back icon
        toolbarBinding.appTitle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        toolbarBinding.menuIcon.load("file:///android_asset/hamberger.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        toolbarBinding.appTitle.load("file:///android_asset/appname_header.svg") {
            decoderFactory(SvgDecoder.Factory())

        }
        toolbarBinding.userIcon.load("file:///android_asset/user.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        toolbarBinding.cartIcon.load("file:///android_asset/shoppingcart.svg") {
            decoderFactory(SvgDecoder.Factory())
        }



        toolbarBinding.cartBadge.apply {
            CartManager.cartLiveData.observe(this@MainActivity) { cartItems ->
                if (cartItems.isNotEmpty()) {
                    visibility = View.VISIBLE
                    text = cartItems.size.toString()
                    animateCartBadge(this)
                } else {
                    visibility = View.GONE
                }
            }
        }

        toolbarBinding.cartIcon.setOnClickListener {
            if (CartManager.getItems().isNotEmpty()) {
                navigateTo(ShoppingCartFragment.newInstance(), true)
            }
        }

        toolbarBinding.cartBadge.setOnClickListener {
            if (CartManager.getItems().isNotEmpty()) {
                navigateTo(ShoppingCartFragment.newInstance(), true)
            }
        }

        toolbarBinding.userIcon.setOnClickListener {
            if (prefs.isLoggedIn) {
                showLogoutPopup()
            } else {
                AuthPromptDialog(
                    activity = this,
                    txtString = "Please log in/sign up to add items to your cart",
                    onRegisterClicked = { startActivity(Intent(this, SignUpActivity::class.java)) },
                    onSignInClicked = { startActivity(Intent(this, SignInActivity::class.java)) }
                ).show()
            }
        }
    }

    private fun setupNavigationDrawer() {
        val headerView = binding.navView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.textViewName)
        val tvMobile = headerView.findViewById<TextView>(R.id.textViewMobile)
        val close = headerView.findViewById<ImageView>(R.id.closeIcon)

        tvName.text = prefs.name ?: "Guest User"
        tvMobile.text = prefs.mobile.orEmpty()
        val versionName = BuildConfig.VERSION_NAME
        findViewById<TextView>(R.id.textViewVersion).text = "Version $versionName"

        close.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        if (!prefs.userAdrress.isNullOrBlank()) {
            binding.deliveryLocation.text = prefs.userAdrress
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navigateTo(HomeFragment.newInstance(address), addToBackStack = true)
                    true
                }

                R.id.nav_orders -> {
                    if (prefs.isLoggedIn) {
                        navigateTo(
                            MyOrdersHistoryFragment.newInstance(prefs.custid.toString()),
                            addToBackStack = true
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Please log in/sign up to view items",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                R.id.nav_shopbycategory -> {
                    navigateTo(ShopByCategoryFragment.newInstance(address), addToBackStack = true)
                    true
                }
                /*R.id.nav_Privacy -> {
                    val url = "https://www.example.com"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)

                    true
                }
                R.id.nav_terms_of_use -> {
                    val url = "https://www.example.com"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)

                    true
                }*/

                else -> false
            }.also {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupClickListeners() {
        binding.deliveryLocation.setOnClickListener {
            if (!prefs.isLoggedIn) {
                AuthPromptDialog(
                    activity = this,
                    txtString = "Please log in to save/access your addresses",
                    onRegisterClicked = { startActivity(Intent(this, SignUpActivity::class.java)) },
                    onSignInClicked = { startActivity(Intent(this, SignInActivity::class.java)) }
                ).show()
            }
        }
    }

    private fun showLogoutPopup() {
        val view = layoutInflater.inflate(R.layout.logout_popup, null)
        val popupWindow = PopupWindow(view, 300, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = true

        popupWindow.showAtLocation(toolbarBinding.userIcon, Gravity.TOP or Gravity.END, 40, 30)

        view.findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            popupWindow.dismiss()
            prefs.clearSession()
            CartManager.clearCart()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun navigateTo(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.simpleName)
        }

        transaction.commit()
        updateToolbarNavigation()

    }

    private fun showCitySpinnerPopup(anchorView: View) {
        if (cities.isEmpty()) return
        val dialog = CityPickerDialogFragment(
            cities = cities,
            prefs = prefs,
            warehousesList = warehousesList
        ) { selected ->
            binding.deliveryLocation.text = SpannableStringBuilder(selected)
            prefs.citySelected = true
            warehouseViewModel.onCitySelected(selected, warehousesList)
        }
        dialog.show(supportFragmentManager, "CityPickerDialog")
    }

    private fun showCityPopupIfNeeded() {
        if (!prefs.citySelected || prefs.userAdrress.isNullOrBlank() || from == "SignUp" ) {
            if (cities.isNotEmpty()) {
                val rootView = window.decorView.findViewById<View>(android.R.id.content)
                rootView.post { showCitySpinnerPopup(rootView) }
            }
        }
    }

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }


            supportFragmentManager.backStackEntryCount > 0 -> {
                supportFragmentManager.popBackStack()
                updateToolbarNavigation()
            }

            else -> super.onBackPressed()
        }
    }

    override fun onCartItemCountChanged(count: Int) {
        toolbarBinding.cartBadge.apply {
            visibility = if (count == 0) View.GONE else View.VISIBLE
            text = count.toString()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionManager.handlePermissionsResult(
            this,
            requestCode,
            permissions,
            grantResults,
            onAllGranted = {
                prefs.hasPermissionBeenRequestedOnce = true
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
                //city current city is matching with list of cities if not then show popup
                // otherwise diretly show the city name on deliveryto
                getCurrentCityOnly()

            },
            onSomeDenied = { denied ->
                if (denied.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    denied.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) {
                    if (!prefs.citySelected) {
                        showCityPopupIfNeeded()
                    }
                }
            }
        )
    }

    private fun animateCartBadge(badgeView: TextView) {
        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            badgeView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.8f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.8f)
        ).apply {
            duration = 750
            interpolator = AccelerateDecelerateInterpolator()
        }

        val alphaFlash = ObjectAnimator.ofFloat(badgeView, View.ALPHA, 2f, 0.6f, 2f).apply {
            duration = 750
        }

        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            badgeView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.8f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.8f, 1f)
        ).apply {
            duration = 750
            interpolator = BounceInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(AnimatorSet().apply { playTogether(scaleUp, alphaFlash) }, scaleDown)
            start()
        }
    }

    private fun updateToolbarNavigation() {
        val showBackButton = supportFragmentManager.backStackEntryCount > 0
        if (showBackButton) {
            toolbarBinding.menuIcon.setImageResource(R.drawable.ic_back_arrow) // Replace with your back icon
            toolbarBinding.menuIcon.visibility = View.VISIBLE
            toolbarBinding.menuIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        } else {
            toolbarBinding.menuIcon.visibility = View.VISIBLE
            toolbarBinding.menuIcon.load("file:///android_asset/hamberger.svg") {
                decoderFactory(SvgDecoder.Factory())
            }
            toolbarBinding.menuIcon.setOnClickListener {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
            }
        }
    }

    private fun getCurrentCityOnly() {
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

                if (Geocoder.isPresent()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                            val addressList = geocoder.getFromLocation(lat, lng, 1)
                            if (!addressList.isNullOrEmpty()) {
                                val city = addressList[0].locality ?: "Unknown"

                                withContext(Dispatchers.Main) {
                                    Log.d(
                                        "CITIES==",
                                        "getCurrentCityOnly: " + cities.size + "CITY" + city
                                    )
                                    if (!city.isNullOrEmpty()) {
                                        val isMatch = cities.any {
                                            it.equals(
                                                city,
                                                ignoreCase = true
                                            )
                                        }
                                        if (isMatch) {
                                            Log.d("CityCheck", "City $city is supported")
                                            prefs.citySelected = true
                                            prefs.userAdrress = city
                                            binding.deliveryLocation.text =
                                                SpannableStringBuilder(city)

                                            warehouseViewModel.onCitySelected(city, warehousesList)

                                        } else {
                                            Log.d("CityCheck", "City $city is NOT supported"+prefs.citySelected)
                                           if (!prefs.citySelected) {
                                                showCityPopupIfNeeded()
                                            }else if(from=="SignUp"){
                                               showCityPopupIfNeeded()

                                           }

                                        }
                                    }

                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Unable to get city",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Geocoder failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Couldn't fetch location", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
