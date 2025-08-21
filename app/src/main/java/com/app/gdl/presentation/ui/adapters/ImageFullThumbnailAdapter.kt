package com.app.gdl.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.bumptech.glide.Glide

class ImageFullThumbnailAdapter(
    private val imageUrls: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<ImageFullThumbnailAdapter.ThumbViewHolder>() {

    inner class ThumbViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.imageLargeThumbnail)

        fun bind(url: String) {
            Glide.with(imageView.context)
                .load(url)
                .centerCrop()
                .into(imageView)

            imageView.setOnClickListener {
                onImageClick(url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_large_thumbnail, parent, false)
        return ThumbViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size
}
