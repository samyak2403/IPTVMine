package com.samyak2403.iptvmine.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.samyak2403.iptvmine.R
import com.samyak2403.iptvmine.model.Channel
import com.samyak2403.iptvmine.screens.PlayerActivity

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "channel_live_notifications"
        private const val CHANNEL_NAME = "Live Channel Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications when your favorite channels go live"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showChannelLiveNotification(channel: Channel) {
        // Create intent to open PlayerActivity with this channel
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("channel", channel)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            channel.name.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tv)
            .setContentTitle("${channel.name} is Live! 📺")
            .setContentText("Tap to watch now")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${channel.name} is now broadcasting. Tap to start watching!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.play_icon,
                "Watch Now",
                pendingIntent
            )
            .build()

        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(
                channel.name.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
