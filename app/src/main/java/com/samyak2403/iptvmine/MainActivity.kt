package com.samyak2403.iptvmine

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.samyak2403.iptvmine.databinding.ActivityMainBinding
import com.samyak2403.iptvmine.screens.AboutFragment
import com.samyak2403.iptvmine.screens.HomeFragment
import com.samyak2403.iptvmine.utils.ThemeManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragmentIndex = 0
    private var isSearchVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup custom status bar color
        setupStatusBar()
        
        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Only apply left, right, and bottom padding to main layout
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            
            // Set status bar background height to match system status bar
            // This prevents toolbar from overlapping with status bar
            binding.statusBarBackground.updateLayoutParams {
                height = systemBars.top
            }
            
            insets
        }

        setSupportActionBar(binding.toolbar)

        // Setup search icon click
        binding.searchIcon.setOnClickListener {
            toggleSearch()
        }

        // Initialize with HomeFragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), 0)
            binding.bottomBar.itemActiveIndex = 0
        }

        // Set up SmoothBottomBar item selection listener
        binding.bottomBar.setOnItemSelectedListener { position ->
            when (position) {
                0 -> {
                    if (currentFragmentIndex != 0) {
                        replaceFragment(HomeFragment(), 0)
                        updateToolbarForFragment(0)
                    }
                }
                1 -> {
                    if (currentFragmentIndex != 1) {
                        replaceFragment(AboutFragment(), 1)
                        updateToolbarForFragment(1)
                    }
                }
            }
        }
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

    private fun toggleSearch() {
        isSearchVisible = !isSearchVisible
        if (isSearchVisible) {
            binding.searchEditText.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.GONE
            binding.searchEditText.requestFocus()
        } else {
            binding.searchEditText.visibility = View.GONE
            binding.toolbarTitle.visibility = View.VISIBLE
            binding.searchEditText.text.clear()
        }
    }

    private fun updateToolbarForFragment(index: Int) {
        when (index) {
            0 -> {
                binding.toolbarTitle.text = "IPTVmine"
                binding.searchIcon.visibility = View.VISIBLE
            }
            1 -> {
                binding.toolbarTitle.text = "About"
                binding.searchIcon.visibility = View.GONE
                binding.searchEditText.visibility = View.GONE
                isSearchVisible = false
            }
        }
    }

    fun getSearchEditText(): EditText = binding.searchEditText

    // Function to replace the current fragment
    private fun replaceFragment(fragment: Fragment, index: Int) {
        currentFragmentIndex = index
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainerView, fragment)
        }
    }



    override fun onBackPressed() {
        if (isSearchVisible) {
            toggleSearch()
        } else if (currentFragmentIndex != 0) {
            // If not on home, go back to home
            binding.bottomBar.itemActiveIndex = 0
            replaceFragment(HomeFragment(), 0)
            updateToolbarForFragment(0)
        } else {
            // If on home, exit app
            super.onBackPressed()
        }
    }
}
