package com.app.gdl.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.gdl.databinding.ActivityImageViewerBinding
import com.app.gdl.presentation.ui.adapters.ImageFullThumbnailAdapter
import com.app.gdl.presentation.ui.adapters.ImageSliderAdapter

class ImageViewerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGES = "extra_images"
        const val EXTRA_POSITION = "extra_position"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val images = intent.getStringArrayListExtra(EXTRA_IMAGES) ?: emptyList<String>()
        val startPosition = intent.getIntExtra(EXTRA_POSITION, 0)

        val adapter = ImageSliderAdapter(
            imageUrls = images,
            openFullScreenOnClick = false,
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(startPosition, false)
        binding.recyclerThumbnails.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerThumbnails.adapter = ImageFullThumbnailAdapter(images.filter { it.contains("_thumbnail") }) { selectedImageUrl ->
            val index = images.filter { it.contains("_medium") }.indexOf(selectedImageUrl)
            if (index != -1) {
                binding.viewPager.setCurrentItem(index, true)
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val recyclerView = binding.viewPager.getChildAt(0) as? RecyclerView
                val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
                        as? ImageSliderAdapter.SliderViewHolder
                viewHolder?.resetZoom()
            }
        })

    }
}
