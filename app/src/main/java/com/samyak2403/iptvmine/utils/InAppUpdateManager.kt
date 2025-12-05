package com.samyak2403.iptvmine.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.material.snackbar.Snackbar

/**
 * Manager class for handling in-app updates
 * Supports both immediate and flexible update flows
 */
class InAppUpdateManager(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var updateInfo: AppUpdateInfo? = null
    
    companion object {
        private const val TAG = "InAppUpdateManager"
        private const val DAYS_FOR_FLEXIBLE_UPDATE = 3
    }

    // Listener for flexible update progress
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytesToDownload = state.totalBytesToDownload()
                Log.d(TAG, "Downloading: $bytesDownloaded / $totalBytesToDownload")
            }
            InstallStatus.DOWNLOADED -> {
                Log.d(TAG, "Download completed. Prompting user to install.")
                popupSnackbarForCompleteUpdate()
            }
            InstallStatus.INSTALLED -> {
                Log.d(TAG, "Update installed successfully")
                unregisterListener()
            }
            InstallStatus.FAILED -> {
                Log.e(TAG, "Update failed with error code: ${state.installErrorCode()}")
                unregisterListener()
            }
            else -> {
                Log.d(TAG, "Install status: ${state.installStatus()}")
            }
        }
    }

    /**
     * Check for available updates and start update flow if available
     * @param updateResultLauncher ActivityResultLauncher for handling update flow
     * @param preferImmediate If true, will prefer immediate update over flexible
     */
    fun checkForUpdate(
        updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        preferImmediate: Boolean = false
    ) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            updateInfo = info
            
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Log.d(TAG, "Update available")
                
                // Determine update type based on priority and staleness
                val updateType = when {
                    // High priority updates should be immediate
                    info.updatePriority() >= 4 -> AppUpdateType.IMMEDIATE
                    // If user preference is immediate
                    preferImmediate -> AppUpdateType.IMMEDIATE
                    // If update has been available for a while, make it immediate
                    info.clientVersionStalenessDays() != null && 
                    info.clientVersionStalenessDays()!! >= DAYS_FOR_FLEXIBLE_UPDATE -> AppUpdateType.IMMEDIATE
                    // Otherwise use flexible
                    else -> AppUpdateType.FLEXIBLE
                }
                
                if (info.isUpdateTypeAllowed(updateType)) {
                    startUpdate(info, updateType, updateResultLauncher)
                } else {
                    Log.d(TAG, "Update type $updateType not allowed")
                }
            } else if (info.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                Log.d(TAG, "No update available")
            } else if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Log.d(TAG, "Update already in progress")
                // Resume update if it was interrupted
                startUpdate(info, AppUpdateType.IMMEDIATE, updateResultLauncher)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Failed to check for updates", exception)
        }
    }

    /**
     * Start the update flow
     */
    private fun startUpdate(
        info: AppUpdateInfo,
        updateType: Int,
        updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        try {
            if (updateType == AppUpdateType.FLEXIBLE) {
                // Register listener for flexible updates
                appUpdateManager.registerListener(installStateUpdatedListener)
            }
            
            val updateOptions = AppUpdateOptions.newBuilder(updateType).build()
            
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateResultLauncher,
                updateOptions
            )
            
            Log.d(TAG, "Started ${if (updateType == AppUpdateType.IMMEDIATE) "immediate" else "flexible"} update flow")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start update flow", e)
        }
    }

    /**
     * Check if there's a downloaded update waiting to be installed
     * Call this in onResume to handle flexible updates
     */
    fun checkForDownloadedUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }
    }

    /**
     * Show snackbar to prompt user to complete flexible update
     */
    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            activity.findViewById(android.R.id.content),
            "An update has been downloaded",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") {
                appUpdateManager.completeUpdate()
            }
            show()
        }
    }

    /**
     * Unregister the update listener
     */
    fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    /**
     * Handle the result from update flow
     */
    fun handleUpdateResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "Update flow completed successfully")
            }
            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Update flow cancelled by user")
            }
            else -> {
                Log.e(TAG, "Update flow failed with result code: $resultCode")
            }
        }
    }
}
