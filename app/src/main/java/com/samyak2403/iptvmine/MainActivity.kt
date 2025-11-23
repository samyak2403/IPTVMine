package com.samyak2403.iptvmine

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.samyak2403.iptvmine.screens.AboutFragment
import com.samyak2403.iptvmine.screens.HomeFragment
import com.samyak2403.iptvmine.utils.ThemeManager
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var bottomBar: SmoothBottomBar
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private var currentFragmentIndex = 0
    private var isSearchVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before setContentView
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup status bar color BEFORE setContentView - using bg_color from resources
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.bg_color)
        
        setContentView(R.layout.activity_main)
        
        // Setup status bar appearance AFTER setContentView

        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)
        searchEditText = findViewById(R.id.searchEditText)
        searchIcon = findViewById(R.id.search_icon)
        bottomBar = findViewById(R.id.bottomBar)

        setSupportActionBar(toolbar)

        // Setup search icon click
        searchIcon.setOnClickListener {
            toggleSearch()
        }

        // Initialize with HomeFragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), 0)
            bottomBar.itemActiveIndex = 0
        }

        // Set up SmoothBottomBar item selection listener
        bottomBar.setOnItemSelectedListener { position ->
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

    private fun toggleSearch() {
        isSearchVisible = !isSearchVisible
        if (isSearchVisible) {
            searchEditText.visibility = View.VISIBLE
            toolbarTitle.visibility = View.GONE
            searchEditText.requestFocus()
        } else {
            searchEditText.visibility = View.GONE
            toolbarTitle.visibility = View.VISIBLE
            searchEditText.text.clear()
        }
    }

    private fun updateToolbarForFragment(index: Int) {
        when (index) {
            0 -> {
                toolbarTitle.text = "IPTVmine"
                searchIcon.visibility = View.VISIBLE
            }
            1 -> {
                toolbarTitle.text = "About"
                searchIcon.visibility = View.GONE
                searchEditText.visibility = View.GONE
                isSearchVisible = false
            }
        }
    }

    fun getSearchEditText(): EditText = searchEditText

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
            bottomBar.itemActiveIndex = 0
            replaceFragment(HomeFragment(), 0)
            updateToolbarForFragment(0)
        } else {
            // If on home, exit app
            super.onBackPressed()
        }
    }
}
