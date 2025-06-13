package com.app.gdl.presentation.ui.adapters

import android.content.Intent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.app.gdl.presentation.ui.activity.ImageViewerActivity
import com.bumptech.glide.Glide

class ImageSliderAdapter(
    private val imageUrls: List<String>
) : RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        fun bind(url: String) {
            Glide.with(imageView.context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.pastpopularitem)
                .into(imageView)

            imageView.setOnClickListener {
                val context = imageView.context
                val intent = Intent(context, ImageViewerActivity::class.java).apply {
                    putStringArrayListExtra(ImageViewerActivity.EXTRA_IMAGES, ArrayList(imageUrls))
                    putExtra(ImageViewerActivity.EXTRA_POSITION, bindingAdapterPosition)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return SliderViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    override fun getItemCount(): Int = imageUrls.size
}
