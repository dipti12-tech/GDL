package com.app.gdl.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.app.gdl.data.model.Category
import com.app.gdl.databinding.ActivityGetproductbycategoryBinding
import com.app.gdl.presentation.ui.adapters.ProductAdapter
import com.app.gdl.presentation.viewmodel.ProductViewModel
import com.app.gdl.presentation.viewmodel.SubCategoryViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductByCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGetproductbycategoryBinding
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private val subcategoryViewModel: SubCategoryViewModel by viewModels()

    private val sort = mutableListOf("Price--Low to High", "Price--High to Low","Newest First")
   //private val filters  = mutableListOf("Brand", "Offers")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetproductbycategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getStringExtra("categoryId")
        val categoryName = intent.getStringExtra("categoryName")
        binding.tvTitle.text = categoryName

        setupObservers()

        binding.btnFilters.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnSort.setOnClickListener {
            Toast.makeText(this, "Sort clicked", Toast.LENGTH_SHORT).show()
        }
        productViewModel.products.observe(this) { response ->
            Log.d(
                "ProductsResponse",
                "Size: ${response.list.size}, ImgPath: ${response.s3_img_path}, Status: ${response.status}"
            )
            productAdapter.submitData(response.list, response.s3_img_path ?: "")
        }
        productViewModel.fetchProducts()

        productAdapter = ProductAdapter()
        binding.selectedItems.layoutManager = GridLayoutManager(this, 2)
        binding.selectedItems.adapter = productAdapter
    }

    private fun setupCategoryChips(categories: List<Category>) {
        binding.categoryGroup.removeAllViews()
        for (name in categories) {
            val chip = Chip(this).apply {
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
                Toast.makeText(this, "Selected category: ${chip.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupObservers() {
        subcategoryViewModel.categories.observe(this) {

            Log.d(
                "ProductsResponse",
                "Size: ${it.category_list.size}"
            )
            setupCategoryChips(it.category_list)

        }
        subcategoryViewModel.fetchSubCategories()

    }

}

