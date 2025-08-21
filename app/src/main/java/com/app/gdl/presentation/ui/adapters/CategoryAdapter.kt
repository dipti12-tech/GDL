package com.app.gdl.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.data.model.PopularCategory
import com.app.gdl.databinding.RowPopularcategoryitemBinding
import com.app.gdl.presentation.ui.adapters.FeatureAdapter.OnProductClickListener
import com.app.gdl.presentation.viewmodel.PopularViewModel

class CategoryAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val productViewModel: PopularViewModel,
    private val listener: ChildCategoryAdapter.OnProductListener,
    private val addToCartListener: ChildCategoryAdapter.AddToCartListener,
    private val listenerShopAll: OnProductClickListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categories = mutableListOf<PopularCategory>()

    fun submitList(newList: List<PopularCategory>) {
        categories.addAll(newList)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(val binding: RowPopularcategoryitemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RowPopularcategoryitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.arrowRight.load("file:///android_asset/ic_arrowright.svg") {
            decoderFactory(SvgDecoder.Factory())
        }

        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryTitle.text = category.category_name

        holder.binding.btnShopAll.setOnClickListener {
            listenerShopAll.onProductClicked(
                "", position, category.cat_id.toString(),
                category.category_name,
                listOf()
            )
        }
        val productAdapter = ChildCategoryAdapter(
            listener = listener,
            addtocartlistener = addToCartListener
        )

        val filteredList = category.products.filter {
            it.item_price?.value != null && it.item_price.value != 0.0 && it.ItemStatus?.value.equals(
                "Active",
                ignoreCase = true
            )
        }
        if (filteredList.size != 0) {
            productAdapter.submitData(list = filteredList, imgPath = "")
            holder.binding.productsRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = productAdapter
            }
            holder.binding
        } else {
            holder.binding.categoryTitle.visibility = View.GONE
            holder.binding.productsRecyclerView.visibility = View.GONE
            holder.binding.btnShopAll.visibility = View.GONE
        }

    }

}
