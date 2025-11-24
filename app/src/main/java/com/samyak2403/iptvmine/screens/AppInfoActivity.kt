package com.samyak2403.iptvmine.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.databinding.ActivityAppInfoBinding
import com.samyak2403.iptvmine.models.Contributor
import com.samyak2403.iptvmine.utils.RequestNetwork
import com.samyak2403.iptvmine.utils.ThemeManager
import com.samyak2403.iptvmine.views.ContributorItemView

class AppInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setContentView
        ThemeManager.applyTheme(this)
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        binding = ActivityAppInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Setup custom status bar color using Google's recommended approach
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
            
            // Add top margin to AppBarLayout to position it below status bar
            binding.appBarLayout.updateLayoutParams<androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams> {
                topMargin = systemBars.top
            }

            insets
        }
        setupToolbar()
        setupContent()

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
            title = "About App"
        }
    }

    private fun setupContent() {
        // Credits Card - Opens LiveTVCollector GitHub repository
        binding.cardCredits.setOnClickListener {
            val url = "https://github.com/bugsfreeweb/LiveTVCollector"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Load contributors from GitHub API
        loadContributors()
    }

    private fun loadContributors() {
        val requestNetwork = RequestNetwork()
        val apiUrl = "https://api.github.com/repos/samyak2403/IPTVMine/contributors"

        requestNetwork.startRequestNetwork(
            RequestNetwork.GET,
            apiUrl,
            "contributors",
            object : RequestNetwork.RequestListener {
                override fun onResponse(
                    tag: String,
                    response: String,
                    responseHeaders: HashMap<String, Any>
                ) {
                    try {
                        val gson = Gson()
                        val contributorType = object : TypeToken<List<Contributor>>() {}.type
                        val contributors: List<Contributor> = gson.fromJson(response, contributorType)

                        Log.i("AppInfoActivity", "Contributors loaded: ${contributors.size}")

                        // Add contributors to the layout
                        binding.layoutContributors.removeAllViews()
                        contributors.forEach { contributor ->
                            val contributorView = ContributorItemView(this@AppInfoActivity)
                            contributorView.setContributor(contributor.login, contributor.avatar_url)
                            contributorView.setOnClickListener {
                                openUrl(contributor.html_url)
                            }
                            binding.layoutContributors.addView(contributorView)
                        }
                    } catch (e: Exception) {
                        Log.e("AppInfoActivity", "Error parsing contributors: ${e.message}")
                    }
                }

                override fun onErrorResponse(tag: String, message: String) {
                    Log.e("AppInfoActivity", "Error loading contributors: $message")
                }
            }
        )
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
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
