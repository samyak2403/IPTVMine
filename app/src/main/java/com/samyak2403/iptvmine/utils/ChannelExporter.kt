package com.samyak2403.iptvmine.utils

import android.content.Context
import com.samyak2403.iptvmine.model.Channel
import java.io.File
import java.io.FileWriter

/**
 * Utility for exporting channels to M3U format
 */
object ChannelExporter {

    /**
     * Export channels to M3U file
     */
    fun exportToM3U(context: Context, channels: List<Channel>, fileName: String = "exported_channels.m3u"): File? {
        return try {
            val file = File(context.getExternalFilesDir(null), fileName)
            val writer = FileWriter(file)

            writer.write("#EXTM3U\n")

            channels.forEach { channel ->
                writer.write("#EXTINF:-1 ")
                writer.write("tvg-logo=\"${channel.logoUrl}\" ")
                writer.write("group-title=\"${channel.category}\",")
                writer.write("${channel.name}\n")
                writer.write("${channel.streamUrl}\n")
            }

            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Export channels by category
     */
    fun exportByCategory(context: Context, channels: List<Channel>, category: String): File? {
        val filteredChannels = channels.filter { it.category == category }
        return exportToM3U(context, filteredChannels, "channels_${category.replace(" ", "_")}.m3u")
    }

    /**
     * Get export file path
     */
    fun getExportPath(context: Context, fileName: String): String {
        return File(context.getExternalFilesDir(null), fileName).absolutePath
    }
}
