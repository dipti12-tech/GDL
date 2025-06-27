package com.app.gdl.presentation.ui.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Category
import com.app.gdl.data.model.PriceResponse
import com.app.gdl.databinding.RowPoupularcategoriesBinding
import com.app.gdl.presentation.ui.adapters.FeatureAdapter.OnProductClickListener
import com.app.gdl.presentation.viewmodel.DefaultPriceViewModel
import com.app.gdl.presentation.viewmodel.ProductViewModel


class PopularCategoryAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val productViewModel: ProductViewModel,
    private val listener: OnProductClickListener,
    private val productlistener: ProductAdapter.OnProductListener,
    private val addToCartListener : ProductAdapter.AddToCartListener

    ) : RecyclerView.Adapter<PopularCategoryAdapter.ViewHolder>() {

    private var categoryList = listOf<Category>()
    private val pageSize = 3
    private lateinit var context: Context

    // NEW: Store page state per category
    private val categoryPageMap = mutableMapOf<String, Int>()

    fun submitList(list: List<Category>, context: Context) {
        this.categoryList = list
        this.context = context
        categoryPageMap.clear() // reset page tracking
        list.forEach { categoryPageMap[it.cat_id.toString()] = 0 } // default page 0
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
        val catId = category.cat_id.toString()
        val currentPage = categoryPageMap[catId] ?: 0

        with(holder.binding) {
            categoryTitle.text = category.category_name
            popularRecyclerView.layoutManager = GridLayoutManager(context, 2)
            val adapter = ProductAdapter(productlistener,addToCartListener)
            popularRecyclerView.adapter = adapter

            // Load products for this specific category and page
            loadProductsForCategory(catId, currentPage, adapter,btnShopall)

            btnShopall.setOnClickListener {
              /*  val intent = Intent(root.context, ProductByCategoryActivity::class.java)
                intent.putExtra("categoryId", catId)
                intent.putExtra("categoryName", category.category_name)
                root.context.startActivity(intent)*/
                listener.onProductClicked(category.cat_id.toString(), category.category_name)

            }

            btnLeft.setOnClickListener {
                val current = categoryPageMap[catId] ?: 0
                if (current > 0) {
                    val updatedPage = current - 1
                    categoryPageMap[catId] = updatedPage
                    loadProductsForCategory(catId, updatedPage, adapter, btnShopall)
                }
            }

            btnRight.setOnClickListener {
                val updatedPage = (categoryPageMap[catId] ?: 0) + 1
                categoryPageMap[catId] = updatedPage
                loadProductsForCategory(catId, updatedPage, adapter, btnShopall)
            }
        }
    }

    private fun loadProductsForCategory(
        categoryId: String,
        page: Int,
        adapter: ProductAdapter,
        btnShopall: AppCompatButton
    ) {
        productViewModel.products.observe(lifecycleOwner) { response ->
            if (categoryList.get(page).cat_id.toString()== categoryId) return@observe // Ensure category match

            val list = response.list
            val start = page * pageSize
            val end = minOf(start + pageSize, list.size)
            val subList = if (start < end) list.subList(start, end) else emptyList()


            adapter.submitData(subList, response.s3_img_path ?: "")
            Log.d("ProductsResponse", "Loaded ${subList.size} items for category $categoryId (page $page)--"+response.s3_img_path)
            btnShopall.visibility = if (subList.size==0) View.GONE else View.VISIBLE

        }

        productViewModel.fetchProducts(categoryId)

    }

    override fun getItemCount(): Int = categoryList.size
}
