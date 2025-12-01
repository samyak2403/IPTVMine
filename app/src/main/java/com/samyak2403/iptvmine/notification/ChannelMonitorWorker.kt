package com.samyak2403.iptvmine.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.samyak2403.iptvmine.model.Channel
import com.samyak2403.iptvmine.provider.ChannelsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ChannelMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ChannelMonitorWorker"
        const val WORK_NAME = "channel_monitor_work"
    }

    private val notificationHelper = NotificationHelper(context)
    private val prefs = context.getSharedPreferences("channel_monitor", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting channel monitoring...")

            // Check battery level
            if (!isBatteryOk()) {
                Log.d(TAG, "Battery too low, skipping check")
                return@withContext Result.success()
            }

            // Check network
            if (!isNetworkAvailable()) {
                Log.d(TAG, "No network available, skipping check")
                return@withContext Result.retry()
            }

            // Get all channels
            val channels = getChannelsFromM3U()
            
            if (channels.isEmpty()) {
                Log.d(TAG, "No channels found")
                return@withContext Result.success()
            }

            Log.d(TAG, "Checking ${channels.size} channels...")

            // Check each channel
            var liveChannelsFound = 0
            channels.forEach { channel ->
                if (isChannelLive(channel)) {
                    // Check if we already notified for this channel
                    val lastNotified = prefs.getLong("notified_${channel.name}", 0)
                    val currentTime = System.currentTimeMillis()
                    
                    // Only notify if we haven't notified in the last 2 hours
                    if (currentTime - lastNotified > 2 * 60 * 60 * 1000) {
                        notificationHelper.showChannelLiveNotification(channel)
                        prefs.edit().putLong("notified_${channel.name}", currentTime).apply()
                        liveChannelsFound++
                        Log.d(TAG, "Notification sent for: ${channel.name}")
                    }
                }
            }

            Log.d(TAG, "Monitoring complete. Found $liveChannelsFound new live channels")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error monitoring channels", e)
            Result.retry()
        }
    }

    private suspend fun getChannelsFromM3U(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val sourceUrl = "https://bugsfreeweb.github.io/LiveTVCollector/LiveTV/India/LiveTV.m3u"
            val url = java.net.URL(sourceUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            
            if (connection.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                parseM3UFile(content)
            } else {
                connection.disconnect()
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching M3U", e)
            emptyList()
        }
    }

    private fun parseM3UFile(fileText: String): List<Channel> {
        val lines = fileText.split("\n")
        val channelsList = mutableListOf<Channel>()
        
        var name: String? = null
        var logoUrl = "assets/images/ic_tv.png"
        var streamUrl: String? = null
        var category: String? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTINF:") -> {
                    name = extractChannelName(line)
                    logoUrl = extractLogoUrl(line) ?: "assets/images/ic_tv.png"
                    category = extractCategory(line)
                }
                line.trim().isNotEmpty() && line.startsWith("http") -> {
                    streamUrl = line.trim()
                    
                    if (!name.isNullOrEmpty() && !streamUrl.isNullOrEmpty()) {
                        channelsList.add(
                            Channel(
                                name = name,
                                logoUrl = logoUrl,
                                streamUrl = streamUrl,
                                category = category ?: "Uncategorized"
                            )
                        )
                    }
                    
                    name = null
                    logoUrl = "assets/images/ic_tv.png"
                    category = null
                }
            }
        }
        
        return channelsList
    }

    private fun extractChannelName(line: String): String? {
        val commaIndex = line.lastIndexOf(",")
        return if (commaIndex != -1 && commaIndex < line.length - 1) {
            line.substring(commaIndex + 1).trim()
        } else null
    }

    private fun extractLogoUrl(line: String): String? {
        val parts = line.split("\"")
        return parts.firstOrNull { it.startsWith("http") }
    }

    private fun extractCategory(line: String): String? {
        val lowerLine = line.lowercase()
        var index = lowerLine.indexOf("group-title=")
        if (index != -1) {
            val startQuote = line.indexOf('"', index)
            if (startQuote != -1) {
                val endQuote = line.indexOf('"', startQuote + 1)
                if (endQuote != -1) {
                    return line.substring(startQuote + 1, endQuote).trim()
                }
            }
        }
        return "Uncategorized"
    }

    private suspend fun isChannelLive(channel: Channel): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(8, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .build()

                val request = Request.Builder()
                    .url(channel.streamUrl)
                    .get() // Use GET to check actual content
                    .addHeader("User-Agent", "IPTVmine/1.0")
                    .build()

                val response = client.newCall(request).execute()
                
                // Check response code
                if (!response.isSuccessful) {
                    response.close()
                    return@withContext false
                }
                
                // Check content type for valid streaming formats
                val contentType = response.header("Content-Type") ?: ""
                val isValidStream = contentType.contains("mpegurl", ignoreCase = true) ||
                                   contentType.contains("x-mpegURL", ignoreCase = true) ||
                                   contentType.contains("application/vnd.apple.mpegurl", ignoreCase = true) ||
                                   contentType.contains("video/", ignoreCase = true) ||
                                   contentType.contains("application/dash+xml", ignoreCase = true) ||
                                   contentType.contains("octet-stream", ignoreCase = true)
                
                // Read first few bytes to verify actual content
                val bodyBytes = response.body?.bytes()?.take(512)?.toByteArray() ?: byteArrayOf()
                response.close()
                
                // Check if content looks like valid stream data
                val hasContent = bodyBytes.isNotEmpty()
                val contentString = String(bodyBytes)
                val isM3U8 = contentString.contains("#EXTM3U") || contentString.contains("#EXT-X-")
                val hasVideoData = bodyBytes.size > 100 // Minimum size for valid stream
                
                val isLive = isValidStream && hasContent && (isM3U8 || hasVideoData)
                
                if (isLive) {
                    Log.d(TAG, "✓ Channel ${channel.name} is LIVE (ContentType: $contentType)")
                } else {
                    Log.d(TAG, "✗ Channel ${channel.name} is OFFLINE")
                }
                
                isLive
            } catch (e: Exception) {
                Log.d(TAG, "Channel ${channel.name} check failed: ${e.message}")
                false
            }
        }
    }

    private fun isBatteryOk(): Boolean {
        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) 
            as? android.os.BatteryManager
        val batteryLevel = batteryManager?.getIntProperty(
            android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
        ) ?: 100
        return batteryLevel > 15
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
