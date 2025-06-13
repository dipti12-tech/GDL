package com.app.gdl.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.databinding.ActivityImageViewerBinding
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

        val adapter = ImageSliderAdapter(images)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(startPosition, false)
    }
}
