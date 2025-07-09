package com.app.gdl.presentation.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.app.gdl.R
import com.app.gdl.data.model.Category
import com.app.gdl.data.model.User
import com.app.gdl.databinding.ActivityGetproductbycategoryBinding
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.viewmodel.DefaultPriceViewModel
import com.app.gdl.presentation.viewmodel.ProductViewModel
import com.app.gdl.presentation.viewmodel.SubCategoryViewModel
import com.app.gdl.utils.SharedPref
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductByCategoryFragment : Fragment(),ProductAdapter.OnProductListener,ProductAdapter.AddToCartListener {

    private var _binding: ActivityGetproductbycategoryBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private val subcategoryViewModel: SubCategoryViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryId: String
    private val defaultPriceViewModel: DefaultPriceViewModel by viewModels()
    var priceclass = ""
    private var customer: User? = null
    lateinit var prefs: SharedPref
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
        val categoryName = arguments?.getString("categoryName")
        binding.tvTitle.text = categoryName

        setupObservers()

        binding.btnFilters.setOnClickListener {
            Toast.makeText(requireContext(), "Filter clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnSort.setOnClickListener {
            Toast.makeText(requireContext(), "Sort clicked", Toast.LENGTH_SHORT).show()
        }

        productViewModel.products.observe(viewLifecycleOwner) { response ->
            Log.d(
                "ProductsResponse",
                "Size: ${response.list.size}, ImgPath: ${response.s3_img_path}, Status: ${response.status}"
            )
            productAdapter.submitData(response.list, response.s3_img_path ?: "")
        }

        if (categoryId.isNotEmpty()) {
            productViewModel.fetchProducts(categoryId)
        }

        productAdapter = ProductAdapter(this,this)
        binding.selectedItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.selectedItems.adapter = productAdapter

    }

    private fun setupCategoryChips(categories: List<Category>) {
        binding.categoryGroup.removeAllViews()
        for (name in categories) {
            val chip = Chip(requireContext()).apply {
                text = name.category_name
                isCheckable = true
                isClickable = true
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.END

                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            binding.categoryGroup.addView(chip)
        }

        binding.categoryGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            chip?.let {
                Toast.makeText(requireContext(), "Selected category: ${chip.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        subcategoryViewModel.categories.observe(viewLifecycleOwner) {
            Log.d("ProductsResponse", "Size: ${it.category_list.size}")
            setupCategoryChips(it.category_list)
        }
        subcategoryViewModel.fetchSubCategories(categoryId)


        // call the default price api with priceclass for login user MLD CBD
        customer = prefs.getCustomerFromPrefs(requireContext())
        priceclass = customer?.address?.get(0)?.price_class.toString()
        Log.d("priceclass", "onCreate: " + priceclass)

        if (priceclass.isNotEmpty() && prefs.isLoggedIn) {
            Log.d("Check Category", "PriceClass: $priceclass, LoggedIn: ${prefs.isLoggedIn}")

            val inventoryIds = prefs.getInventoryIds(requireContext())
            Log.d("SIZE IN PREF IDS Category", inventoryIds.size.toString())

            // Call this BEFORE observe
            defaultPriceViewModel.fetchDefaultPrice(priceclass)

            defaultPriceViewModel.defaultPrice.observe(viewLifecycleOwner) { response ->
                Log.d("ResponseWith Price Category", "Received: $response")

                val groupedByPrice = response.list.groupBy { it.CustomerPriceClass.value }
                Log.d("groupedByPrice Category", "Received: $groupedByPrice")

                val priceItems = groupedByPrice[priceclass] ?: emptyList()
                Log.d("priceItems Category", "Received: $priceItems")

                val filteredPriceItems =
                    priceItems.filter { inventoryIds.contains(it.InventoryID.value) }

                Log.d("FilteredItems Category", filteredPriceItems.toString())
                productAdapter.setPriceMap(filteredPriceItems)

            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(categoryId: String, categoryName: String) = ProductByCategoryFragment().apply {
            arguments = Bundle().apply {
                putString("categoryId", categoryId)
                putString("categoryName", categoryName)
            }
        }
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
