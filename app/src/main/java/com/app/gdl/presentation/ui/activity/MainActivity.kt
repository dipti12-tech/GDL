package com.app.gdl.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.app.gdl.R
import com.app.gdl.databinding.ActivityMainBinding
import com.app.gdl.databinding.ToolbarHeaderBinding
import com.app.gdl.presentation.ui.fragment.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbarBinding: ToolbarHeaderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Toolbar
        toolbarBinding = binding.toolbarHeader
        setSupportActionBar(toolbarBinding.customToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)  // since you use ImageView title

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

               /* R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, ProfileFragment())
                        .commit()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }*/

                else -> false
            }
        }

        // Load default fragment when activity starts
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, HomeFragment())
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