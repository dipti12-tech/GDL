package com.app.gdl.presentation.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.databinding.ActivityProductdetailsBinding
import com.app.gdl.presentation.ui.adapters.ImageSliderAdapter
import com.app.gdl.presentation.ui.adapters.ImageThumbnailAdapter
import com.app.gdl.presentation.viewmodel.ProductDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.log10

@AndroidEntryPoint
class ProductByCategoryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductdetailsBinding
    private val viewModel: ProductDetailViewModel by viewModels()
    var quantity = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductdetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val inventoryId = intent.getStringExtra("inventory_id")

        if (inventoryId != null) {
            viewModel.fetchProductDetail(inventoryId)
        }
        binding.btnPlus.setOnClickListener {
            incrementQuantity()
        }
        binding.btnMinus.setOnClickListener {
            decrementQuantity()
        }
        viewModel.products.observe(this) { response ->
            val item = response.details.firstOrNull() ?: return@observe
            binding.tvTitle.text = item.CustomName?.value ?: "No Name"
            binding.tvDescription.text = item.CustomDescription?.value ?: "No Description"

            val basePath = response.s3_img_path
            val inventoryId = item.InventoryID?.value ?: ""
            val mainImage = item.ImageUrl?.value
            val additionalImages = item.Images?.value ?: emptyList()


            val allImages = mutableListOf<String>()
            mainImage?.let { allImages.add(it) }
            allImages.addAll(additionalImages.filter { it.contains("_thumbnail") })

            val imageUrls = allImages.map { "$basePath$inventoryId/images/$it" }
            val pagerAdapter = ImageSliderAdapter(imageUrls)
            binding.viewPagerImages.adapter = pagerAdapter

            binding.recyclerThumbnails.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            Log.d("SIZEa", "onCreate: "+ imageUrls.toString())
            binding.recyclerThumbnails.adapter =
                ImageThumbnailAdapter(imageUrls.filter { it.contains("_thumbnail") }) { selectedImageUrl ->
                    val index = imageUrls.indexOf(selectedImageUrl)
                    if (index != -1) {
                        binding.viewPagerImages.setCurrentItem(index, true)
                    }
                }

        }
    }

    fun incrementQuantity() {
        if (quantity < 1000) {
            quantity++
        }
        binding.tvQuantity.text ="Qty : " +quantity

    }

    fun decrementQuantity() {
        if (quantity > 1) {
            quantity--
        }
        binding.tvQuantity.text ="Qty : " +quantity

    }
}