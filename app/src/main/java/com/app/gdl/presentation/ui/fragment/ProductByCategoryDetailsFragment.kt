package com.app.gdl.presentation.ui.fragment

import ItemPrice
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.data.model.CartItem
import com.app.gdl.data.model.Category
import com.app.gdl.data.model.ProductListItem
import com.app.gdl.data.model.User
import com.app.gdl.databinding.ActivityProductdetailsBinding
import com.app.gdl.presentation.ui.activity.SignInActivity
import com.app.gdl.presentation.ui.activity.SignUpActivity
import com.app.gdl.presentation.ui.adapters.ImageSliderAdapter
import com.app.gdl.presentation.ui.adapters.ImageThumbnailAdapter
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.ui.adapters.UomPriceAdapter
import com.app.gdl.presentation.viewmodel.ProductDetailViewModel
import com.app.gdl.utils.AuthPromptDialog
import com.app.gdl.utils.CartManager
import com.app.gdl.utils.SharedPref
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductByCategoryDetailsFragment : Fragment(), ProductAdapter.AddToCartListener {

    private var _binding: ActivityProductdetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()

    private var quantity = 1
    private var selectedUom: ItemPrice? = null
    private var mainImageUrl: String = ""
    private var categoryIds: String = ""
    private var priceItems: List<ItemPrice> = emptyList()
    private lateinit var sharedPref: SharedPref
    var priceclass = ""
    private var customer: User? = null
    private var isProductDetailsLoaded = false
    private var isPricesLoaded = false
    var list: List<ProductListItem> = emptyList()

    companion object {
        fun newInstance(inventoryId: String) = ProductByCategoryDetailsFragment().apply {
            arguments = Bundle().apply {
                Log.d("inventoryId***", "inventoryId: $inventoryId")
                putString("inventory_id", inventoryId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProductdetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = SharedPref(requireContext())
        val inventoryId = arguments?.getString("inventory_id").orEmpty()
        Log.d("inventoryId", "ProductByCategoryDetailsFragment: $inventoryId")
        binding.progressBar.visibility = View.VISIBLE
        isProductDetailsLoaded = false
        isPricesLoaded = false
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
        if (sharedPref.isLoggedIn) {
            binding.llCreateAccount.visibility = View.GONE
        }
        binding.imgRightarrow.load("file:///android_asset/triangle_right.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        binding.rightarrow.load("file:///android_asset/triangle_right.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        customer = sharedPref.getCustomerFromPrefs(requireContext())
        priceclass = customer?.address?.get(0)?.price_class.toString()

        if (sharedPref.isLoggedIn && priceclass != "null") {
            Log.d(
                "sharedPref.default_price===IFFF",
                "onViewCreated: " + sharedPref.default_price.toString() + priceclass
            )
            viewModel.fetchProductDetail(inventoryId, priceclass)
        } else {
            Log.d(
                "sharedPref.default_price===",
                "onViewCreated: " + sharedPref.default_price.toString()
            )
            viewModel.fetchProductDetail(inventoryId, sharedPref.default_price.toString())
        }

        binding.btnPlus.setOnClickListener { incrementQuantity(selectedUom?.UOM?.value) }
        binding.btnMinus.setOnClickListener { decrementQuantity(selectedUom?.UOM?.value) }

        binding.btnAddToCart.setOnClickListener {

            if (sharedPref.isLoggedIn) {
                if (selectedUom == null) {
                    Log.w("AddToCart", "Please select a UOM first")
                    return@setOnClickListener
                }

                Log.d("mainImageUrl =====", "onViewCreated: " + mainImageUrl)
                val selectedItem =
                    CartItem(
                        inventoryId = inventoryId,
                        name = binding.tvTitle.text.toString(),
                        imageUrl = mainImageUrl,
                        unit = binding.spinnerSize.selectedItem as String,
                        quantity = quantity,
                        pricePerUnit = selectedUom!!.Price?.value!!,
                        BaseUOM = selectedUom!!.UOM?.value.toString(),
                        category = categoryIds

                    )

                Log.d("TAGDipti", "onViewCreated: " + selectedUom!!.UOM?.value.toString())
                addToCartClicked(selectedItem)
                Toast.makeText(
                    context,
                    "Added ${binding.tvTitle.text.toString()} to cart",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //show the popup must logged in
                AuthPromptDialog(
                    activity = requireActivity(),
                    txtString = "Please log in to save/access your addresses",
                    onRegisterClicked = {
                        // Navigate to Register screen
                        startActivity(Intent(requireActivity(), SignUpActivity::class.java))
                    },
                    onSignInClicked = {
                        // Navigate to Login screen
                        startActivity(Intent(requireActivity(), SignInActivity::class.java))
                    }
                ).show()

            }
        }


        viewModel.products.observe(viewLifecycleOwner, Observer { response ->
            val savedJson = sharedPref.getString("CATEGORY_LIST", "")
            val categoryType = object : TypeToken<List<Category>>() {}.type
            val savedList: List<Category> = Gson().fromJson(savedJson, categoryType)
            Log.d("GETCATEGORY", "onViewCreated: " + savedList.toString())

            val item = response.details.firstOrNull() ?: return@Observer
            priceItems = item.item_price!!

            val matchedCategory = savedList.find {
                it.cat_id.toString() == item.CustomCategory?.value

            }
            val categoryId = matchedCategory?.cat_id
            categoryIds = matchedCategory?.cat_id.toString()
            val categoryName = matchedCategory?.category_name
            Log.d("categoryName", categoryName.toString())
            binding.tvCategory.text = categoryName

            binding.tvCategory.setOnClickListener {

                //call category page
                val fragment = ProductByCategoryFragment.newInstance(
                    "",
                    0,
                    categoryId.toString(),
                    categoryName.toString(),
                    list
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_content, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            binding.tvTitle.text = item.CustomName?.value ?: "No Name"
            binding.tvCurrent.text = item.CustomName?.value ?: "No Name"
            binding.tvDescription.text = item.CustomDescription?.value ?: "No Description"

            val basePath = response.s3_img_path
            val productId = item.InventoryID?.value ?: ""
            val mainImage = item.ImageUrl?.value
            val additionalImages = item.Images?.value ?: emptyList()
            mainImageUrl = if (!mainImage.isNullOrEmpty()) {
                "$basePath$productId/images/$mainImage"
            } else {
                ""
            }

            binding.tvHome.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
            binding.headerSignup.setOnClickListener {
                startActivity(Intent(context, SignUpActivity::class.java))
            }
            binding.imgLogo.load("file:///android_asset/artoftech_logo.svg") {
                decoderFactory(SvgDecoder.Factory())
            }

            val allImages = mutableListOf<String>()

            allImages.addAll(additionalImages.filter { it.contains("_thumbnail") })

            val imageUrls = allImages.map { "$basePath$productId/images/$it" }

            val pagerAdapter = ImageSliderAdapter(imageUrls)
            binding.viewPagerImages.adapter = pagerAdapter

            binding.recyclerThumbnails.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            binding.recyclerThumbnails.adapter =
                ImageThumbnailAdapter(imageUrls.filter { it.contains("_thumbnail") }) { selectedImageUrl ->
                    val index = imageUrls.indexOf(selectedImageUrl)
                    if (index != -1) {
                        binding.viewPagerImages.setCurrentItem(index, true)
                    }
                }
            isProductDetailsLoaded = true
            isPricesLoaded = true
            checkIfLoadingIsComplete()
            val adapter = UomPriceAdapter(priceItems)
            binding.recycleviewUOM.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.recycleviewUOM.adapter = adapter

            val uomList = priceItems.map { it.UOM?.value }.toSet().toList()

            if (uomList.isNotEmpty()) {
                Log.d("uomList@@", "onViewCreated: " + uomList.size + uomList.get(0).toString())
                val spinnerAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uomList)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSize.adapter = spinnerAdapter
            }

            binding.spinnerSize.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedUom = priceItems.get(position)

                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        selectedUom = null
                    }
                }
        })
    }

    private fun incrementQuantity(uom: String?) {
        if (quantity < 1000) {
            quantity++
        }
        binding.tvQuantity.text = "$quantity"
    }

    private fun decrementQuantity(uom: String?) {
        if (quantity > 1) {
            quantity--
        }
        binding.tvQuantity.text = "$quantity"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkIfLoadingIsComplete() {
        if (isProductDetailsLoaded && isPricesLoaded) {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun addToCartClicked(cartItem: CartItem) {
        CartManager.addItem(cartItem)
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
