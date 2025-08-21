package com.app.gdl.presentation.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.data.photosview.PhotoView
import com.app.gdl.presentation.ui.activity.ImageViewerActivity
import com.bumptech.glide.Glide

class ImageSliderAdapter(
    private val imageUrls: List<String>,
    private val openFullScreenOnClick: Boolean = true,
    private val onImageClick: (() -> Unit)? = null
) : RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: PhotoView = itemView.findViewById(R.id.image_src)
        fun bind(url: String) {
            val updatedUrl = url.replace("_thumbnail", "_large")
            Glide.with(imageView.context)
                .load(updatedUrl)
                .fitCenter()
                .placeholder(R.drawable.default_placeholder)
                .into(imageView)

            imageView.setOnClickListener {
                if (openFullScreenOnClick) {
                    val context = imageView.context
                    val intent = Intent(context, ImageViewerActivity::class.java).apply {
                        putStringArrayListExtra(
                            ImageViewerActivity.EXTRA_IMAGES,
                            ArrayList(imageUrls)
                        )
                        putExtra(ImageViewerActivity.EXTRA_POSITION, bindingAdapterPosition)
                    }
                    context.startActivity(intent)
                } else {
                    onImageClick?.invoke()
                }

            }
        }

        fun resetZoom() {
            imageView.scale = 1.0f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.full_screen_layout, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size
}
