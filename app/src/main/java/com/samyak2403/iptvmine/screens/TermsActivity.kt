/*
 * Created by Samyak kamble
 *  Copyright (c) 2024 . All rights reserved.
 */

package com.samyak2403.iptvmine.screens

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.databinding.ActivityTermsBinding
import com.samyak2403.iptvmine.utils.ThemeManager

class TermsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setContentView
        ThemeManager.applyTheme(this)
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup custom status bar color
        setupStatusBar()

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Only apply left, right, and bottom padding to main layout
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)

            // Add top margin to AppBarLayout to position it below status bar
            binding.appBarLayout.updateLayoutParams<androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams> {
                topMargin = systemBars.top
            }

            insets
        }

        setupToolbar()
    }

    /**
     * Setup status bar with custom color
     * Sets status bar color to bg_color and handles light/dark status bar icons
     */
    private fun setupStatusBar() {
        window.apply {
            // Enable drawing behind the status bar
            statusBarColor = getColor(R.color.bg_color)

            // For API 30+, use WindowInsetsController for better control
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // This will be handled by the theme's windowLightStatusBar attribute
                // which automatically adjusts icon colors based on status bar color
            } else {
                // For older APIs, manually set light status bar if needed
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Check if bg_color is light, then use dark icons
                    // For now, assuming dark background, so use light icons (default)
                    decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                } else {
                    decorView.systemUiVisibility
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Terms & Conditions"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
