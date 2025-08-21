package com.app.gdl.presentation.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.Category
import com.app.gdl.data.model.Product
import com.app.gdl.data.model.ProductListItem
import com.app.gdl.databinding.ActivityGetproductbycategoryBinding
import com.app.gdl.presentation.ui.activity.SignUpActivity
import com.app.gdl.presentation.ui.adapters.CategoryCarouselAdapter
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.viewmodel.ProductViewModel
import com.app.gdl.presentation.viewmodel.SubCategoryViewModel
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductByCategoryFragment : Fragment(), ProductAdapter.OnProductListener,
    ProductAdapter.AddToCartListener {

    private var _binding: ActivityGetproductbycategoryBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private val subcategoryViewModel: SubCategoryViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryId: String
    private lateinit var FromCustomList: String
    private lateinit var topBanners: List<ProductListItem>
    lateinit var prefs: SharedPref
    var positionCustom: Int = 0

    companion object {
        fun newInstance(
            customlist: String,
            position: Int,
            categoryId: String,
            categoryName: String,
            topBanners: List<ProductListItem>
        ) = ProductByCategoryFragment().apply {
            arguments = Bundle().apply {
                putString("categoryId", categoryId)
                putInt("position", position)
                putString("categoryName", categoryName)
                putString("FROMCustomList", customlist)
                putParcelableArrayList("topBanners", ArrayList(topBanners))

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityGetproductbycategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SharedPref(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryId = arguments?.getString("categoryId").orEmpty()
        positionCustom = arguments?.getInt("position")!!
        val categoryName = arguments?.getString("categoryName")
        FromCustomList = arguments?.getString("FROMCustomList").orEmpty()
        topBanners = arguments?.getParcelableArrayList<ProductListItem>("topBanners")!!
        topBanners.let {
            Log.d("TAG==topBanners", "onViewCreated: " + topBanners.size)
        }
        CartManager.cartLiveData.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isNotEmpty()) {
                binding.fabViewCart.visibility = View.VISIBLE
                binding.cartItemCount.text = cartItems.size.toString()
                animateCartBadge(binding.cartItemCount)
            } else {
                binding.fabViewCart.visibility = View.GONE
            }
        }

        binding.fabViewCart.setOnClickListener {
            val fragment = ShoppingCartFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(null)
                .commit()
        }
        if (prefs.isLoggedIn) {
            binding.llCreateAccount.visibility = View.GONE
        }
        binding.tvTitle.text = categoryName
        binding.tvCategory.text = categoryName
        binding.tvHome.setOnClickListener {
            val fragment = HomeFragment.newInstance(prefs.userAdrress.toString())
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.imgRightarrow.load("file:///android_asset/triangle_right.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        binding.btnFilters.setOnClickListener {
            subcategoryViewModel.categories.observe(viewLifecycleOwner) {
                Log.d("ProductsResponse", "Size: ${it.category_list.size}")
            }
        }

        binding.btnSort.setOnClickListener {
            Toast.makeText(requireContext(), "Sort clicked", Toast.LENGTH_SHORT).show()
        }
        binding.headerSignup.setOnClickListener {
            startActivity(Intent(context, SignUpActivity::class.java))
        }
        binding.imgLogo.load("file:///android_asset/artoftech_logo.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        productAdapter = ProductAdapter(this, this)
        binding.selectedItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.selectedItems.adapter = productAdapter


        if (FromCustomList != null && FromCustomList.equals("FROMCustomList")) {
            val filteredList = if (topBanners.isNotEmpty()) {
                topBanners[positionCustom].products.orEmpty().filter {
                    it.item_price?.value != null && it.item_price.value != 0.0
                            && it.ItemStatus?.value.equals("Active", ignoreCase = true)
                }
            } else {
                emptyList()
            }
            if (filteredList.size != 0) {
                binding.categoryCarousel.visibility = View.GONE
                binding.selectedItems.visibility = View.VISIBLE
                prefs.s3_img_path?.let {
                    productAdapter.submitCustomData(
                        FromCustomList, filteredList,
                        it
                    )
                }

            } else {
                binding.selectedItems.visibility = View.GONE
                binding.noDataText.visibility = View.VISIBLE
                binding.noDataText.text = "No products found"
            }
        } else {
            setupObservers()

        }
    }

    private fun setupObservers() {
        subcategoryViewModel.categories.observe(viewLifecycleOwner) {
            if (it.category_list.size != 0) {
                setupCategoryChips(it.category_list)
            } else {
                binding.categoryCarousel.visibility = View.GONE
            }
        }

        subcategoryViewModel.fetchSubCategories(categoryId)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onProductDataClicked(inventoryId: String) {
        val fragment = ProductByCategoryDetailsFragment.newInstance(inventoryId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun addToCartClicked(item: CartItem) {
        CartManager.addItem(item)
    }


    fun setupCategoryChips(categoryList: List<Category>) {
        val allCategory = Category(
            cat_id = categoryId.toInt(), category_name = "All",
            category_desc = "",
            category_img_path = "",
            parent_cat_id = categoryId.toInt(),
            status = 0,
            created_at = ""
        )
        val modifiedCategoryList = mutableListOf<Category>().apply {
            add(allCategory)
            addAll(categoryList)
        }
        val adapter = CategoryCarouselAdapter(modifiedCategoryList) { selectedCategory ->
            Log.d("Category", "Selected: ${selectedCategory.category_name}")
            val categoryId = selectedCategory.cat_id

            //check here verified priceclass
            val customer = prefs.getCustomerFromPrefs(requireContext())
            val priceclass = customer?.address?.getOrNull(0)?.price_class.orEmpty()

            val priceClassToUse = if (prefs.isLoggedIn && priceclass.isNotEmpty()) {
                priceclass
            } else {
                prefs.default_price
            }
            if (priceClassToUse != null) {
                productViewModel.fetchProducts(categoryId.toString(), priceClassToUse)
            }
        }

        productViewModel.products.observe(viewLifecycleOwner) { response ->
            val filteredList = response.list.filter {
                it.item_price.value != 0.0 &&
                        it.ItemStatus.value.equals("Active", ignoreCase = true)
            }

            if (filteredList.size != 0 && filteredList.isNotEmpty()) {
                binding.selectedItems.visibility = View.VISIBLE
                binding.noDataText.visibility = View.GONE
                productAdapter.submitData(filteredList, response.s3_img_path ?: "")
            } else {
                binding.selectedItems.visibility = View.GONE
                binding.noDataText.visibility = View.VISIBLE
                binding.noDataText.text = "No products found"
            }
        }

        binding.categoryCarousel.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryCarousel.adapter = adapter

        binding.categoryCarousel.onFlingListener = null
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.categoryCarousel)

        if (categoryList.isNotEmpty()) {
            adapter.selectCategoryAt(0)
        }
    }

    fun animateCartBadge(badgeView: TextView) {
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

        val scaleUpAndAlpha = AnimatorSet().apply {
            playTogether(scaleUp, alphaFlash)
        }

        AnimatorSet().apply {
            playSequentially(scaleUpAndAlpha, scaleDown)
            start()
        }
    }

}
