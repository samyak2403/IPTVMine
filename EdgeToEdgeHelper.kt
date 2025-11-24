package com.samyak.tempboxlite.Utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.EdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Helper class for implementing edge-to-edge design with Android 15+ compatibility
 *
 * This class provides methods to properly handle edge-to-edge layouts, replace deprecated
 * status bar APIs, and ensure compatibility with Android 15+ requirements.
 *
 * @author Samyak Kamble
 * @version 1.0
 */
object EdgeToEdgeHelper {

    /**
     * Enable edge-to-edge display with proper inset handling for Android 15+ compatibility
     * This replaces the deprecated setStatusBarColor and setNavigationBarColor APIs
     *
     * @param activity The activity to enable edge-to-edge for
     */
    fun enableEdgeToEdge(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= 35) {
            // Android 15+ - Use the new EdgeToEdge API (required for apps targeting SDK 35+)
            EdgeToEdge.enable(activity)
        } else {
            // Pre-Android 15 - Use manual edge-to-edge setup for backward compatibility
            setupManualEdgeToEdge(activity)
        }

        // Additional configuration for better compatibility across all versions
        setupWindowInsets(activity)
        setupStatusBarAppearance(activity)
    }

    /**
     * Manual edge-to-edge setup for devices running Android versions prior to 15
     * Uses modern APIs only - no deprecated methods
     *
     * @param activity The activity to setup manual edge-to-edge for
     */
    private fun setupManualEdgeToEdge(activity: ComponentActivity) {
        val window = activity.window

        // Use WindowCompat for modern edge-to-edge without deprecated APIs
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Apply edge-to-edge using modern window insets approach
        val decorView = window.decorView
        ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->
            // Let system bars be transparent by consuming the insets properly
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Setup window insets for proper edge-to-edge layout
     *
     * @param activity The activity to setup insets for
     */
    private fun setupWindowInsets(activity: Activity) {
        val window = activity.window

        // Make sure we handle display cutouts properly (replaces deprecated LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        // Enable drawing under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /**
     * Setup status bar appearance without using deprecated APIs
     *
     * @param activity The activity to setup status bar appearance for
     */
    private fun setupStatusBarAppearance(activity: Activity) {
        val window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Use the new WindowInsetsController API for Android 11+
            window.insetsController?.apply {
                // Set light status bar icons for dark themes
                setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            }
        } else {
            // Use WindowCompat for older versions
            WindowCompat.getInsetsController(window, window.decorView)
                ?.isAppearanceLightStatusBars = false
        }
    }

    /**
     * Apply proper window insets to a view for edge-to-edge layout
     *
     * @param view The root view to apply insets to
     */
    fun applySystemWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Apply system window insets with custom bottom padding (useful for activities with bottom navigation)
     *
     * @param view The root view to apply insets to
     * @param maintainBottomPadding Whether to maintain bottom padding for navigation bars
     */
    fun applySystemWindowInsetsWithBottomNav(view: View, maintainBottomPadding: Boolean) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomPadding = if (maintainBottomPadding) systemBars.bottom else 0
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }
    }

    /**
     * Handle edge-to-edge for activities with drawer layout
     *
     * @param activity The activity with drawer layout
     * @param rootView The root view of the activity
     */
    fun setupEdgeToEdgeWithDrawer(activity: ComponentActivity, rootView: View) {
        enableEdgeToEdge(activity)

        // Apply insets to root view
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Only apply top and horizontal insets for drawer layouts
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    /**
     * Enable immersive mode for specific activities (like splash screen)
     * Uses only modern APIs - no deprecated SYSTEM_UI_FLAG constants
     *
     * @param activity The activity to enable immersive mode for
     */
    fun enableImmersiveMode(activity: ComponentActivity) {
        enableEdgeToEdge(activity)

        val window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Modern API for Android 11+
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Use WindowCompat for older versions (no deprecated flags)
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Apply immersive behavior using modern insets controller
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    /**
     * Check if the device is running Android 15+ and requires edge-to-edge by default
     *
     * @return true if Android 15+ and targeting SDK 35+
     */
    fun requiresEdgeToEdge(): Boolean {
        return Build.VERSION.SDK_INT >= 35 // Android 15 (API 35)
    }

    /**
     * Get information about the current edge-to-edge implementation
     * Useful for debugging and logging
     *
     * @return String describing which edge-to-edge method is being used
     */
    fun getEdgeToEdgeInfo(): String {
        return if (Build.VERSION.SDK_INT >= 35) {
            "Using EdgeToEdge API for Android 15+ (API ${Build.VERSION.SDK_INT})"
        } else {
            "Using manual edge-to-edge for Android < 15 (API ${Build.VERSION.SDK_INT})"
        }
    }

    /**
     * Enable edge-to-edge with logging for debugging purposes
     *
     * @param activity The activity to enable edge-to-edge for
     * @param tag Tag for logging (usually activity class name)
     */
    fun enableEdgeToEdgeWithLogging(activity: ComponentActivity, tag: String) {
        android.util.Log.d(tag, "Enabling edge-to-edge: ${getEdgeToEdgeInfo()}")
        enableEdgeToEdge(activity)
    }

    /**
     * Apply modern theming without deprecated APIs
     * This replaces manual status bar color setting
     *
     * @param activity The activity to apply theming to
     */
    fun applyModernTheming(activity: ComponentActivity) {
        enableEdgeToEdge(activity)

        // Let the system handle the colors based on the app theme
        // This is the recommended approach for Android 15+
        // Note: Setting transparent colors is no longer needed with EdgeToEdge.enable()
        // The EdgeToEdge API automatically handles system bar transparency
    }

    /**
     * Setup MainActivity with modern edge-to-edge (NO deprecated APIs)
     * Uses only WindowCompat and EdgeToEdge APIs
     *
     * @param activity The MainActivity instance
     * @param rootView The root view to apply insets to
     */
    fun setupMainActivityWithColorTheme(activity: ComponentActivity, rootView: View) {
        if (Build.VERSION.SDK_INT >= 35) {
            // Android 15+ - Use EdgeToEdge API (required)
            EdgeToEdge.enable(activity)
        } else {
            // Pre-Android 15 - Use modern WindowCompat approach ONLY
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        // Setup window insets and appearance
        setupWindowInsets(activity)
        setupStatusBarAppearance(activity)

        // Apply insets to root view for drawer layout with colorTheme background
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Set background color to colorTheme instead of using deprecated setStatusBarColor
            val colorTheme = ContextCompat.getColor(activity, com.samyak.tempboxlite.R.color.colorTheme)
            v.setBackgroundColor(colorTheme)

            // Only apply top and horizontal insets for drawer layouts
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
}
