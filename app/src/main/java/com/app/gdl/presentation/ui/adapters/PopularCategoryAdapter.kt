package com.app.gdl.presentation.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Category
import com.app.gdl.databinding.RowPoupularcategoriesBinding
import com.app.gdl.presentation.viewmodel.ProductViewModel

class PopularCategoryAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val productViewModel: ProductViewModel
) : RecyclerView.Adapter<PopularCategoryAdapter.ViewHolder>() {

    private var categoryList = listOf<Category>()
    private var currentPage = 0
    private val pageSize = 3
    private lateinit var context: Context

    fun submitList(list: List<Category>, context: Context) {
        this.categoryList = list
        this.context = context
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowPoupularcategoriesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowPoupularcategoriesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        val productsRecyclerView = holder.binding.popularRecyclerView

        with(holder.binding) {
            categoryTitle.text = category.category_name
            productsRecyclerView.layoutManager = GridLayoutManager(context, 2)

            val adapter = ProductAdapter()
            productsRecyclerView.adapter = adapter

            loadProductsForCategory(category.cat_id, adapter)

            btnLeft.setOnClickListener {
                if (currentPage > 0) {
                    currentPage--
                    loadProductsForCategory(category.cat_id, adapter)
                }
            }

            btnRight.setOnClickListener {
                currentPage++
                loadProductsForCategory(category.cat_id, adapter)
            }
        }
    }

    private fun loadProductsForCategory(categoryId: Int, adapter: ProductAdapter) {
        productViewModel.fetchProducts()
        productViewModel.products.observe(lifecycleOwner) { response ->
            val list = response.list
            val start = currentPage * pageSize
            val end = minOf(start + pageSize, list.size)
            val subList = if (start < end) list.subList(start, end) else emptyList()

            adapter.submitData(subList, response.s3_img_path ?: "")

            Log.d("ProductsResponse", "Loaded ${subList.size} items for category $categoryId")
        }
    }

    override fun getItemCount(): Int = categoryList.size
}
