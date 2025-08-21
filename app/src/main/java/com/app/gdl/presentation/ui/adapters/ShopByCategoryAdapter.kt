package com.app.gdl.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Category
import com.app.gdl.databinding.RowShopbycategorylistBinding
import com.app.gdl.presentation.ui.adapters.FeatureAdapter.OnProductClickListener
import com.bumptech.glide.Glide

class ShopByCategoryAdapter(
    private val listener: OnProductClickListener
) :
    RecyclerView.Adapter<ShopByCategoryAdapter.ViewHolder>() {

    private var categoryList = listOf<Category>()
    fun submitList(list: List<Category>) {
        categoryList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowShopbycategorylistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowShopbycategorylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        with(holder.binding) {
            categoryNameTextView.text = category.category_name

            Glide.with(categoryImageView.context)
                .load(category.category_img_path)
                .into(categoryImageView)
            categoryImageView.setOnClickListener {
                listener.onProductClicked(
                    "", position, category.cat_id.toString(),
                    category.category_name,
                    listOf()
                )

            }
        }
    }

    override fun getItemCount() = categoryList.size
}

