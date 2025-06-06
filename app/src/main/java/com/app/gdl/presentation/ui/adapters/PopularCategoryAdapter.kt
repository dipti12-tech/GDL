package com.app.gdl.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Category
import com.app.gdl.databinding.RowPastpopularlistBinding
import com.app.gdl.databinding.RowPoupularcategoriesBinding
import com.bumptech.glide.Glide

class PopularCategoryAdapter :
    RecyclerView.Adapter<PopularCategoryAdapter.ViewHolder>() {

    private var categoryList = listOf<Category>()
    fun submitList(list: List<Category>) {
        categoryList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowPoupularcategoriesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularCategoryAdapter.ViewHolder {
        val binding =
            RowPoupularcategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        with(holder.binding) {
            categoryTitle.text = category.category_name
        }
    }

    override fun getItemCount() = categoryList.size
}
