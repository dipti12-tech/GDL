package com.app.gdl.presentation.ui.fragment

import android.os.Bundle
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
import com.app.gdl.databinding.ActivityGetproductbycategoryBinding
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.viewmodel.ProductViewModel
import com.app.gdl.presentation.viewmodel.SubCategoryViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityGetproductbycategoryBinding.inflate(inflater, container, false)
        return binding.root
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
