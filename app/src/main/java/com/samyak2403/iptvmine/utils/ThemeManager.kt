package com.samyak2403.iptvmine.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * ThemeManager - Manages app theme (Light/Dark mode)
 */
object ThemeManager {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode_enabled"

    /**
     * Check if dark mode is enabled
     */
    fun isDarkModeEnabled(context: Context): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * Enable or disable dark mode
     */
    fun setDarkMode(context: Context, enabled: Boolean) {
        val prefs = getPreferences(context)
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        applyTheme(enabled)
    }

    /**
     * Apply the theme based on saved preference
     */
    fun applyTheme(context: Context) {
        val isDarkMode = isDarkModeEnabled(context)
        applyTheme(isDarkMode)
    }

    /**
     * Apply theme immediately
     */
    private fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    /**
     * Toggle dark mode on/off
     */
    fun toggleDarkMode(context: Context): Boolean {
        val currentMode = isDarkModeEnabled(context)
        val newMode = !currentMode
        setDarkMode(context, newMode)
        return newMode
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
