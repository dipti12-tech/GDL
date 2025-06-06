package com.app.gdl.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.databinding.ActivityProductdetailsBinding

class ProductByCategDetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProductdetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductdetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}