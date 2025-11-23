package com.samyak2403.iptvmine.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages favorite channels
 */
object FavoriteManager {
    private const val PREFS_NAME = "favorites"
    private const val KEY_FAVORITES = "favorite_channels"
    private const val DELIMITER = "|||"

    /**
     * Check if channel is favorite
     */
    fun isFavorite(context: Context, channelName: String): Boolean {
        return getFavorites(context).contains(channelName)
    }

    /**
     * Add channel to favorites
     */
    fun addFavorite(context: Context, channelName: String) {
        val favorites = getFavorites(context).toMutableSet()
        favorites.add(channelName)
        saveFavorites(context, favorites)
    }

    /**
     * Remove channel from favorites
     */
    fun removeFavorite(context: Context, channelName: String) {
        val favorites = getFavorites(context).toMutableSet()
        favorites.remove(channelName)
        saveFavorites(context, favorites)
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(context: Context, channelName: String): Boolean {
        return if (isFavorite(context, channelName)) {
            removeFavorite(context, channelName)
            false
        } else {
            addFavorite(context, channelName)
            true
        }
    }

    /**
     * Get all favorites
     */
    fun getFavorites(context: Context): Set<String> {
        val prefs = getPrefs(context)
        val favoritesString = prefs.getString(KEY_FAVORITES, null)
        
        return if (favoritesString.isNullOrEmpty()) {
            emptySet()
        } else {
            favoritesString.split(DELIMITER).filter { it.isNotBlank() }.toSet()
        }
    }

    /**
     * Clear all favorites
     */
    fun clearFavorites(context: Context) {
        getPrefs(context).edit().remove(KEY_FAVORITES).apply()
    }

    private fun saveFavorites(context: Context, favorites: Set<String>) {
        val prefs = getPrefs(context)
        val favoritesString = favorites.joinToString(DELIMITER)
        prefs.edit().putString(KEY_FAVORITES, favoritesString).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
