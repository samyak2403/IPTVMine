package com.samyak2403.iptvmine.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object ChannelMonitorScheduler {

    private const val TAG = "ChannelMonitorScheduler"

    fun scheduleMonitoring(context: Context) {
        Log.d(TAG, "Scheduling automatic channel monitoring...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val monitoringRequest = PeriodicWorkRequestBuilder<ChannelMonitorWorker>(
            30, TimeUnit.MINUTES, // Repeat every 30 minutes
            5, TimeUnit.MINUTES   // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                15, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ChannelMonitorWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            monitoringRequest
        )

        Log.d(TAG, "Channel monitoring scheduled successfully")
    }

    fun cancelMonitoring(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ChannelMonitorWorker.WORK_NAME)
        Log.d(TAG, "Channel monitoring cancelled")
    }

    fun isMonitoringScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(ChannelMonitorWorker.WORK_NAME)
            .get()
        return workInfos.any { !it.state.isFinished }
    }
}
