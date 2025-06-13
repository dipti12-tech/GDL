package com.app.gdl.presentation.ui.adapters

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.bumptech.glide.Glide

class ImageThumbnailAdapter(
    private val imageUrls: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<ImageThumbnailAdapter.ThumbViewHolder>() {

    inner class ThumbViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        fun bind(url: String) {
            Glide.with(imageView.context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.pastpopularitem)
                .into(imageView)

            imageView.setOnClickListener {
                onImageClick(url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(120, 120).apply {
                marginEnd = 16
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ThumbViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ThumbViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size
}
