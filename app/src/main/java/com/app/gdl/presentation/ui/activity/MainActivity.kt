package com.app.gdl.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import coil.decode.SvgDecoder
import coil.load
import com.app.gdl.R
import com.app.gdl.databinding.ActivityMainBinding
import com.app.gdl.databinding.ToolbarHeaderBinding
import com.app.gdl.presentation.ui.fragment.HomeFragment
import com.app.gdl.utils.SharedPref
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarBinding: ToolbarHeaderBinding
    var address: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prefs = SharedPref(this)

        address = intent.getStringExtra("addressUser").toString()
        // Set Toolbar
        toolbarBinding = binding.toolbarHeader
        setSupportActionBar(toolbarBinding.customToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbarBinding.menuIcon.load("file:///android_asset/hamberger.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        toolbarBinding.userIcon.load("file:///android_asset/user.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        toolbarBinding.cartIcon.load("file:///android_asset/shoppingcart.svg") {
            decoderFactory(SvgDecoder.Factory())
        }
        // Menu icon opens/closes drawer
        toolbarBinding.menuIcon.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        // User Icon click listener (if needed)
        toolbarBinding.userIcon.setOnClickListener {
            // You can navigate to user profile or show a dialog here
        }

        // Cart Icon click listener (if needed)
        toolbarBinding.cartIcon.setOnClickListener {
            // Navigate to cart screen or fragment
        }


        // Set up NavigationView menu item selection
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, HomeFragment())
                        .commit()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_logout -> {
                    prefs.isLoggedIn = false
                    intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        if (savedInstanceState == null) {
            val fragment = HomeFragment.newInstance(address)
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}