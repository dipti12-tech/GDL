package com.app.gdl.presentation.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.data.model.Category
import com.app.gdl.databinding.RowPastpopularlistBinding
import com.bumptech.glide.Glide

class MostFamousCategory :
    RecyclerView.Adapter<MostFamousCategory.ViewHolder>() {

    private var categoryList = listOf<Category>()
    fun submitList(list: List<Category>) {
        categoryList = list
        notifyDataSetChanged()
    }
    inner class ViewHolder(val binding: RowPastpopularlistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MostFamousCategory.ViewHolder {
        val binding = RowPastpopularlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        Log.d("TAG", "onBindViewHolder: MostFamous ")
        with(holder.binding) {
            productTitle.text = category.category_name
            Glide.with(productImage.context)
                .load(category.category_img_path)
                .into(productImage)
        }
    }
    override fun getItemCount() = categoryList.size
}
