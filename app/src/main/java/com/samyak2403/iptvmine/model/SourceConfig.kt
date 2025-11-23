package com.samyak2403.iptvmine.model

/**
 * Data class representing a source configuration from Firebase
 */
data class SourceConfig(
    val id: String = "",
    val url: String = "",
    val name: String = "",
    val enabled: Boolean = true,
    val priority: Int = 0
)
