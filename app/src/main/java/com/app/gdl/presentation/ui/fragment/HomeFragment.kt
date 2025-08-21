package com.app.gdl.presentation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.ProductListItem
import com.app.gdl.databinding.FragmentHomeBinding
import com.app.gdl.presentation.ui.activity.SignInActivity
import com.app.gdl.presentation.ui.activity.SignUpActivity
import com.app.gdl.presentation.ui.adapters.CategoryAdapter
import com.app.gdl.presentation.ui.adapters.ChildCategoryAdapter
import com.app.gdl.presentation.ui.adapters.FeatureAdapter
import com.app.gdl.presentation.ui.adapters.PopularItemsAdapter
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.ui.adapters.ShopByCategoryAdapter
import com.app.gdl.presentation.viewmodel.AllCategoryIdViewModel
import com.app.gdl.presentation.viewmodel.CategoryViewModel
import com.app.gdl.presentation.viewmodel.PopularItemViewModel
import com.app.gdl.presentation.viewmodel.PopularViewModel
import com.app.gdl.presentation.viewmodel.ShopCategoryViewModel
import com.app.gdl.utils.AnalyticsHelper
import com.app.gdl.utils.AuthPromptDialog
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import com.app.gdl.utils.SlotType
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(),
    FeatureAdapter.OnProductClickListener,
    ProductAdapter.OnProductListener,
    ProductAdapter.AddToCartListener,
    ChildCategoryAdapter.OnProductListener,
    ChildCategoryAdapter.AddToCartListener {

    companion object {
        private const val ADDRESS = "addressUser"

        fun newInstance(address: String): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ADDRESS, address)
                }
            }
        }
    }

    private lateinit var binding: FragmentHomeBinding

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val shopCategoryViewModel: ShopCategoryViewModel by viewModels()
    private val popularViewModel: PopularViewModel by viewModels()
    private val popularItemViewModel: PopularItemViewModel by viewModels()
    private val categoryIdsViewModel: AllCategoryIdViewModel by viewModels()


    private lateinit var featureAdapter: FeatureAdapter
    private lateinit var shopByCategoryAdapter: ShopByCategoryAdapter
    private lateinit var popularAdapter: CategoryAdapter
    private lateinit var popularItemsAdapter: PopularItemsAdapter

    private lateinit var middleBannerAdapter: FeatureAdapter
    private lateinit var bottomBannerAdapter: FeatureAdapter

    private lateinit var prefs: SharedPref
    private var topBanners = listOf<ProductListItem>()
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var currentIndex = 0
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val count = featureAdapter.itemCount
            if (count == 0) return

            currentIndex = (currentIndex + 1) % count
            binding.featureRecycleview.smoothScrollToPosition(currentIndex)
            updateDotIndicators(currentIndex)
            autoScrollHandler.postDelayed(this, 3000) // scroll every 3 sec
        }
    }
    private val middleAutoScrollHandler = Handler(Looper.getMainLooper())
    private var middleCurrentIndex = 0
    private val middleAutoScrollRunnable = object : Runnable {
        override fun run() {
            val count = middleBannerAdapter.itemCount
            if (count == 0) return

            middleCurrentIndex = (middleCurrentIndex + 1) % count
            binding.bannerMiddleAdsRecyclerView.smoothScrollToPosition(middleCurrentIndex)
            middleAutoScrollHandler.postDelayed(this, 4000)
        }
    }

    private val bottomAutoScrollHandler = Handler(Looper.getMainLooper())
    private var bottomCurrentIndex = 0
    private val bottomAutoScrollRunnable = object : Runnable {
        override fun run() {
            val count = bottomBannerAdapter.itemCount
            if (count == 0) return

            bottomCurrentIndex = (bottomCurrentIndex + 1) % count
            binding.bannerBottomAdsRecyclerView.smoothScrollToPosition(bottomCurrentIndex)
            bottomAutoScrollHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SharedPref(requireContext())
        AnalyticsHelper.logScreenView(requireActivity(), "Home screen")

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
        CartManager.cartLiveData.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isNotEmpty()) {
                binding.fabViewCart.visibility = View.VISIBLE
                binding.cartItemCount.text = cartItems.size.toString()
            } else {
                binding.fabViewCart.visibility = View.GONE
            }
        }

    }

    private fun setupUi() {
        if (prefs.isLoggedIn) {
            binding.llCreateAccount.visibility = View.GONE
        }
        binding.imgLogo.load("file:///android_asset/artoftech_logo.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        binding.arrowRight.load("file:///android_asset/ic_arrowright.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        binding.cartViewIcon.load("file:///android_asset/shoppingcart.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        binding.fabViewCart.setOnClickListener {
            val fragment = ShoppingCartFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.headerSignup.setOnClickListener {
            startActivity(Intent(context, SignUpActivity::class.java))
        }
        featureAdapter = FeatureAdapter(this)
        middleBannerAdapter = FeatureAdapter(this)
        bottomBannerAdapter = FeatureAdapter(this)

        binding.featureRecycleview.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = featureAdapter

            featureAdapter.itemCount.let { count ->
                if (count > 1) {
                    setupDotIndicators(count)
                    updateDotIndicators(0)
                }else{
                    binding.dotIndicatorLayout.visibility = View.GONE
                }
            }
        }

        binding.featureRecycleview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        currentIndex = position
                        updateDotIndicators(position)
                    }
                }
            }
        })
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.featureRecycleview)


        binding.bannerMiddleAdsRecyclerView.apply {

            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = middleBannerAdapter

            middleBannerAdapter.itemCount.let { count ->
                if (count > 1) {
                    setupDotIndicators(count)
                    updateDotIndicators(0)
                }else{
                    binding.dotIndicatorLayout.visibility = View.GONE
                }
            }
            middleAutoScrollHandler.postDelayed(middleAutoScrollRunnable, 4000)

        }

        binding.bannerBottomAdsRecyclerView.apply {

            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bottomBannerAdapter

            bottomBannerAdapter.itemCount.let { count ->
                if (count > 1) {
                    setupDotIndicators(count)
                    updateDotIndicators(0)
                }else{
                    binding.dotIndicatorLayout.visibility = View.GONE
                }
            }
            bottomAutoScrollHandler.postDelayed(bottomAutoScrollRunnable, 5000)
        }

        shopByCategoryAdapter = ShopByCategoryAdapter(this)
        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = shopByCategoryAdapter
        }

        popularItemsAdapter = PopularItemsAdapter(this, this)
        binding.PastandPopularRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = popularItemsAdapter
        }
    }

    private fun setupDotIndicators(count: Int) {
        binding.dotIndicatorLayout.removeAllViews()
        val dotSize = resources.getDimensionPixelSize(R.dimen.dot_size)
        val margin = resources.getDimensionPixelSize(R.dimen.dot_margin)

        repeat(count) { i ->
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply {
                    setMargins(margin, 0, margin, 0)
                }
                setBackgroundResource(R.drawable.dot_unselected)
                isClickable = true
                setOnClickListener {
                    binding.featureRecycleview.smoothScrollToPosition(i)
                    currentIndex = i
                    updateDotIndicators(i)
                }
            }
            binding.dotIndicatorLayout.addView(dot)
        }
    }

    private fun updateDotIndicators(index: Int) {
        for (i in 0 until binding.dotIndicatorLayout.childCount) {
            val dot = binding.dotIndicatorLayout.getChildAt(i)

            val isSelected = i == index
            dot.setBackgroundResource(
                if (isSelected) R.drawable.dot_selected else R.drawable.dot_unselected
            )
        }
    }


    private fun setupObservers() {
        shopCategoryViewModel.fetchFeaturedCategories()

        shopCategoryViewModel.allcategories.observe(viewLifecycleOwner) {
            shopByCategoryAdapter.submitList(it.category_list)
        }
        categoryIdsViewModel.fetchAllCategoriesIds()

        categoryIdsViewModel.allIds.observe(viewLifecycleOwner) {
            val jsonString = Gson().toJson(it.category_list)
            prefs.saveString("CATEGORY_LIST", jsonString)

        }


    }

    override fun onProductClicked(
        customlist: String,
        position: Int,
        categoryId: String,
        categoryName: String,
        topBanners: List<ProductListItem>
    ) {
        val fragment =
            ProductByCategoryFragment.newInstance(
                customlist,
                position,
                categoryId,
                categoryName,
                topBanners
            )
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onProductDataClicked(inventoryId: String) {
        val fragment = ProductByCategoryDetailsFragment.newInstance(inventoryId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun addToCartClicked(cartItem: CartItem) {
        if (prefs.isLoggedIn) {
            CartManager.addItem(cartItem)
        } else {
            AuthPromptDialog(
                activity = requireActivity(),
                txtString = "Please log in/sign up to add items to your cart",
                onRegisterClicked = {
                    startActivity(Intent(requireActivity(), SignUpActivity::class.java))
                },
                onSignInClicked = {
                    startActivity(Intent(requireActivity(), SignInActivity::class.java))
                }
            ).show()
        }
    }

    fun fetchPopularItemsWithNewPriceClass(priceClass: String, warehouseData: String?) {
        popularViewModel.getpopularcategories.removeObservers(viewLifecycleOwner)
        prefs.default_price?.let {
            if (warehouseData != null) {
                categoryViewModel.fetchCustomList(it,warehouseData)
            }
        }
        Log.d("default_price", priceClass)
        popularItemViewModel.fetchPopularItems(priceClass)
        popularItemViewModel.products.observe(viewLifecycleOwner) { response ->
            val filteredList = response.list.filter {
                it.item_price.value != 0.0 && it.ItemStatus.value.equals(
                    "Active",
                    ignoreCase = true
                )
            }
            filteredList.forEach { item ->
                Log.d("FilteredList", item.toString())

            }
            if (filteredList.isNotEmpty()) {
                binding.quickShopTitle.visibility = View.VISIBLE
                binding.tabButtons.visibility =View.VISIBLE
                binding.pastPurchasesBtn.visibility = View.VISIBLE
                binding.PastandPopularRecyclerView.visibility = View.VISIBLE
                binding.arrowRight.visibility = View.VISIBLE
                binding.popularShopAll.visibility = View.VISIBLE
                popularItemsAdapter.submitData(filteredList, response.s3_img_path)
            }
        }

        categoryViewModel.categories.observe(viewLifecycleOwner) { response ->
            val allItems = response.list
            prefs.s3_img_path = response.s3_img_path
            topBanners =
                allItems.filter { it.slot == SlotType.TBL.code || it.slot == SlotType.TBR.code }
            val middleBanners = allItems.filter { it.slot == SlotType.MS.code }
            val bottomBanners = allItems.filter { it.slot == SlotType.BBLS.code || it.slot == SlotType.BBRS.code }

            featureAdapter.submitList(topBanners)
            setupDotIndicators(topBanners.size)
            updateDotIndicators(0)
            currentIndex = 0

            autoScrollHandler.removeCallbacks(autoScrollRunnable)
            autoScrollHandler.postDelayed(autoScrollRunnable, 3000)

            middleBannerAdapter.submitList(middleBanners)
            middleAutoScrollHandler.removeCallbacks(middleAutoScrollRunnable)
            middleAutoScrollHandler.postDelayed(middleAutoScrollRunnable, 4000)

            bottomBannerAdapter.submitList(bottomBanners)
            bottomAutoScrollHandler.removeCallbacks(bottomAutoScrollRunnable)
            bottomAutoScrollHandler.postDelayed(bottomAutoScrollRunnable, 5000)

            binding.featureRecycleview.visibility =
                if (topBanners.isNotEmpty()) View.VISIBLE else View.GONE
            binding.bannerMiddleAdsRecyclerView.visibility =
                if (middleBanners.isNotEmpty()) View.VISIBLE else View.GONE
            binding.bannerBottomAdsRecyclerView.visibility =
                if (bottomBanners.isNotEmpty()) View.VISIBLE else View.GONE
        }
        popularViewModel.fetchGetpopularCategories()

        popularViewModel.getpopularcategories.observe(viewLifecycleOwner) { response ->
            if (response.category_list.isNotEmpty()) {
                response.category_list.forEach {
                    Log.d("PopularCategoryDipti", "Category: ${it.category_name}")
                }
                binding.productsCategoryRecyclerView.visibility = View.VISIBLE

                if (binding.productsCategoryRecyclerView.layoutManager == null) {
                    binding.productsCategoryRecyclerView.layoutManager =
                        LinearLayoutManager(requireContext())
                }

                popularAdapter = CategoryAdapter(
                    lifecycleOwner = viewLifecycleOwner,
                    productViewModel = popularViewModel,
                    listener = this,
                    addToCartListener = this,
                    listenerShopAll = this,
                )
                binding.productsCategoryRecyclerView.adapter = popularAdapter

                popularAdapter.submitList(response.category_list)

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (featureAdapter.itemCount > 0) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
        }
        val customer = prefs.getCustomerFromPrefs(requireActivity())
        val priceclass = customer?.address?.get(0)?.price_class.toString()
        val warehouseData = customer?.address?.get(0)?.warehouse.toString()

        if (prefs.isLoggedIn && priceclass != "null") {
            fetchPopularItemsWithNewPriceClass(priceclass, warehouseData)
        } else {
            fetchPopularItemsWithNewPriceClass(prefs.default_price.toString(), prefs.near_warehouse)
        }
    }

    override fun onPause() {
        super.onPause()
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        middleAutoScrollHandler.removeCallbacks(middleAutoScrollRunnable)
        bottomAutoScrollHandler.removeCallbacks(bottomAutoScrollRunnable)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        autoScrollHandler.removeCallbacks(autoScrollRunnable) // prevent memory leak
        middleAutoScrollHandler.removeCallbacks(middleAutoScrollRunnable)
        bottomAutoScrollHandler.removeCallbacks(bottomAutoScrollRunnable)
    }
}
