package com.app.gdl.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.model.Category
import com.google.android.material.card.MaterialCardView

class CategoryCarouselAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryCarouselAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardCategory)
        val text: TextView = view.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_category_card, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, pos: Int) {
        val category = categories[pos]
        holder.text.text = category.category_name

        val isSelected = selectedPosition == pos
        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, if (isSelected) R.color.blue else R.color.white)
        )
        holder.text.setTextColor(
            ContextCompat.getColor(holder.itemView.context, if (isSelected) R.color.white else R.color.black)
        )
        holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, if (isSelected) R.color.blue else R.color.bg_text)

        holder.card.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = pos
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size
    fun selectCategoryAt(index: Int) {
        if (index in categories.indices) {
            val oldPos = selectedPosition
            selectedPosition = index
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPosition)
            onCategoryClick(categories[index])
        }
    }
}

