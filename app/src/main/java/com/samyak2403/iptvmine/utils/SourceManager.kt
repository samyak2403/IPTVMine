package com.samyak2403.iptvmine.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages IPTV source URLs using SharedPreferences
 */
object SourceManager {
    private const val PREFS_NAME = "iptv_sources"
    private const val KEY_SOURCES = "source_urls"
    private const val DELIMITER = "|||"

    /**
     * Get saved source URLs
     */
    fun getSources(context: Context): List<String> {
        val prefs = getPrefs(context)
        val sourcesString = prefs.getString(KEY_SOURCES, null)
        
        return if (sourcesString.isNullOrEmpty()) {
            getDefaultSources()
        } else {
            sourcesString.split(DELIMITER).filter { it.isNotBlank() }
        }
    }

    /**
     * Save source URLs
     */
    fun saveSources(context: Context, sources: List<String>) {
        val prefs = getPrefs(context)
        val sourcesString = sources.joinToString(DELIMITER)
        prefs.edit().putString(KEY_SOURCES, sourcesString).apply()
    }

    /**
     * Add a new source URL
     */
    fun addSource(context: Context, url: String): Boolean {
        if (url.isBlank() || !StreamUrlUtils.isValidUrlFormat(url)) {
            return false
        }

        val sources = getSources(context).toMutableList()
        if (!sources.contains(url)) {
            sources.add(url)
            saveSources(context, sources)
            return true
        }
        return false
    }

    /**
     * Remove a source URL
     */
    fun removeSource(context: Context, url: String) {
        val sources = getSources(context).toMutableList()
        sources.remove(url)
        saveSources(context, sources)
    }

    /**
     * Clear all sources
     */
    fun clearSources(context: Context) {
        getPrefs(context).edit().remove(KEY_SOURCES).apply()
    }

    /**
     * Reset to default sources
     */
    fun resetToDefaults(context: Context) {
        saveSources(context, getDefaultSources())
    }

    /**
     * Get default source URLs
     */
    private fun getDefaultSources(): List<String> {
        return listOf(
            "https://bugsfreeweb.github.io/LiveTVCollector/LiveTV/India/LiveTV.m3u"
        )
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
