package com.samyak2403.iptvmine.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Get saved language code
        val languageCode = newBase.getSharedPreferences("settings", MODE_PRIVATE)
            .getString("language", "") ?: ""

        // If no language is set, use system default
        if (languageCode.isEmpty()) {
            super.attachBaseContext(newBase)
            return
        }

        // Create locale and update configuration
        val locale = Locale(languageCode)
        val config = Configuration(newBase.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)

        // Create context with updated configuration
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply RTL layout direction for RTL languages
        updateLayoutDirection()
    }

    private fun updateLayoutDirection() {
        val languageCode = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("language", "") ?: ""
        
        // Set RTL layout direction for Arabic
        if (languageCode == "ar") {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }
} 