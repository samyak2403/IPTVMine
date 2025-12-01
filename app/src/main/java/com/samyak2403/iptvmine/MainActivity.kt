package com.samyak2403.iptvmine

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.samyak2403.iptvmine.databinding.ActivityMainBinding
import com.samyak2403.iptvmine.notification.ChannelMonitorScheduler
import com.samyak2403.iptvmine.screens.AboutFragment
import com.samyak2403.iptvmine.screens.HomeFragment
import com.samyak2403.iptvmine.utils.ThemeManager
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragmentIndex = 0
    private var isSearchVisible = false

    companion object {
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    // Voice search launcher
    private val voiceSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val voiceText = matches[0]
                binding.searchEditText.setText(voiceText)
                // Optionally trigger search automatically
                // performSearch(voiceText)
            }
        }
    }

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

        // Setup mic icon click for voice search
        binding.micIcon.setOnClickListener {
            startVoiceSearch()
        }

        // Setup close icon click
        binding.closeIcon.setOnClickListener {
            toggleSearch()
        }

        // Initialize with HomeFragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), 0)
            binding.bottomBar.itemActiveIndex = 0
        }

        // Start automatic channel monitoring
        startAutomaticNotifications()

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
            // Show search mode
            binding.searchEditText.visibility = View.VISIBLE
            binding.closeIcon.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.GONE
            binding.searchIcon.visibility = View.GONE
            binding.micIcon.visibility = View.GONE
            binding.searchEditText.requestFocus()
        } else {
            // Show normal mode
            binding.searchEditText.visibility = View.GONE
            binding.closeIcon.visibility = View.GONE
            binding.toolbarTitle.visibility = View.VISIBLE
            binding.searchIcon.visibility = View.VISIBLE
            binding.micIcon.visibility = View.VISIBLE
            binding.searchEditText.text.clear()
        }
    }

    /**
     * Start voice search using Android's speech recognition
     */
    private fun startVoiceSearch() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search...")
            }
            voiceSearchLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice search not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToolbarForFragment(index: Int) {
        when (index) {
            0 -> {
                binding.toolbarTitle.text = "IPTVmine"
                binding.searchIcon.visibility = View.VISIBLE
                binding.micIcon.visibility = View.VISIBLE
                binding.closeIcon.visibility = View.GONE
                binding.searchEditText.visibility = View.GONE
                isSearchVisible = false
            }
            1 -> {
                binding.toolbarTitle.text = "About"
                binding.searchIcon.visibility = View.GONE
                binding.micIcon.visibility = View.GONE
                binding.closeIcon.visibility = View.GONE
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



    /**
     * Start automatic channel monitoring and notifications
     * This runs automatically when app starts - no user action needed!
     */
    private fun startAutomaticNotifications() {
        Log.d(TAG, "Starting automatic channel notifications...")

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                scheduleChannelMonitoring()
            }
        } else {
            scheduleChannelMonitoring()
        }
    }

    private fun scheduleChannelMonitoring() {
        // Schedule automatic monitoring
        ChannelMonitorScheduler.scheduleMonitoring(this)
        Log.d(TAG, "Automatic channel monitoring started successfully!")
        
        // Check if this is first time showing notification message
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val hasShownNotificationMessage = prefs.getBoolean("notification_message_shown", false)
        
        if (!hasShownNotificationMessage) {
            // Show toast only first time
            Toast.makeText(
                this,
                "Automatic channel notifications enabled",
                Toast.LENGTH_SHORT
            ).show()
            
            // Mark as shown
            prefs.edit().putBoolean("notification_message_shown", true).apply()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleChannelMonitoring()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission denied. You won't receive channel alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
