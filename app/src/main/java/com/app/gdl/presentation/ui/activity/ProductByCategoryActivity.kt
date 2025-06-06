package com.app.gdl.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gdl.databinding.ActivityGetproductbycategoryBinding
import com.app.gdl.presentation.ui.adapters.MostFamousCategory
import com.app.gdl.presentation.viewmodel.MostFamousCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductByCategoryActivity: AppCompatActivity() {
    private lateinit var binding: ActivityGetproductbycategoryBinding
    private lateinit var pastPopularAdapter: MostFamousCategory
    private val popularviewModel: MostFamousCategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetproductbycategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getStringExtra("categoryId")
        val categoryName = intent.getStringExtra("categoryName")
        binding.tvTitle.text=categoryName

        popularviewModel.getpopularcategories.observe(this) { response ->
            Log.d("response**!!", response.status.toString()+"SIZE--"+response.category_list.size)
            pastPopularAdapter.submitList(response.category_list)

            popularviewModel.fetchGetpopularCategories()

            pastPopularAdapter = MostFamousCategory()
            binding.selectedItems.layoutManager = GridLayoutManager(this, 3)
            binding.selectedItems.adapter = pastPopularAdapter
        }
    }
}

