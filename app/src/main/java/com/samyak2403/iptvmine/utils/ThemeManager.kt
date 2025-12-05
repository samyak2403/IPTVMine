package com.samyak2403.iptvmine.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * ThemeManager - App is always in dark mode
 */
object ThemeManager {

    /**
     * Apply dark mode theme
     */
    fun applyTheme(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
