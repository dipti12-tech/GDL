package com.app.gdl.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.databinding.ActivityShoppingcartBinding

class ShoppingCartActivity : AppCompatActivity() {

private lateinit var binding: ActivityShoppingcartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingcartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}