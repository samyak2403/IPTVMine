# In-App Updates Implementation

This document explains the in-app updates feature implemented in IPTVmine using Google Play Core libraries.

## Overview

The app now supports Google Play's in-app updates feature, which allows users to update the app without leaving it. The implementation supports both **Immediate** and **Flexible** update flows.

## Features

### Update Types

1. **Immediate Updates**
   - Full-screen update experience
   - User must update before continuing to use the app
   - Triggered when:
     - Update priority is 4 or higher
     - Update has been available for 3+ days
     - Manually configured to prefer immediate updates

2. **Flexible Updates**
   - Background download while user continues using the app
   - User prompted to restart when download completes
   - Default update type for regular updates

### How It Works

1. **On App Launch**: The app automatically checks for updates when MainActivity starts
2. **Update Available**: If an update is found, the appropriate update flow is triggered
3. **Download Progress**: For flexible updates, progress is tracked in the background
4. **Installation**: User is prompted to restart the app to complete the update

## Implementation Details

### Dependencies Added

```kotlin
implementation("com.google.android.play:app-update:2.1.0")
implementation("com.google.android.play:app-update-ktx:2.1.0")
```

### Key Components

1. **InAppUpdateManager.kt**
   - Manages the entire update lifecycle
   - Handles both immediate and flexible updates
   - Tracks download progress
   - Shows snackbar notifications

2. **MainActivity.kt**
   - Initializes the update manager
   - Checks for updates on app start
   - Handles update results
   - Checks for downloaded updates in onResume()

## Testing

### Testing in Development

In-app updates only work with apps downloaded from Google Play Store. For testing:

1. **Internal Testing Track**
   - Upload your APK/AAB to Google Play Console
   - Create an internal testing track
   - Add test users
   - Install the app from Play Store on test devices
   - Upload a new version with higher versionCode
   - Open the app to trigger the update flow

2. **Using FakeAppUpdateManager** (for unit tests)
   ```kotlin
   val fakeUpdateManager = FakeAppUpdateManager(context)
   fakeUpdateManager.setUpdateAvailable(2) // versionCode 2 available
   ```

### Update Priority

Set update priority in Google Play Console (0-5):
- 0-3: Flexible update (default)
- 4-5: Immediate update

## Configuration

### Change Update Behavior

In `MainActivity.kt`, modify the `checkForAppUpdates()` call:

```kotlin
// For immediate updates by default
inAppUpdateManager.checkForUpdate(
    updateResultLauncher = updateLauncher,
    preferImmediate = true
)

// For flexible updates by default (current setting)
inAppUpdateManager.checkForUpdate(
    updateResultLauncher = updateLauncher,
    preferImmediate = false
)
```

### Adjust Staleness Days

In `InAppUpdateManager.kt`, change the threshold:

```kotlin
companion object {
    private const val DAYS_FOR_FLEXIBLE_UPDATE = 3 // Change this value
}
```

## User Experience

### Flexible Update Flow
1. User opens app
2. Update downloads in background
3. Snackbar appears: "An update has been downloaded" with "RESTART" button
4. User taps RESTART to complete update

### Immediate Update Flow
1. User opens app
2. Full-screen update dialog appears
3. User must update to continue
4. App restarts after update

## Requirements

- Android 5.0 (API level 21) or higher
- App must be installed from Google Play Store
- Supported on: Android mobile, tablets, and ChromeOS devices

## Troubleshooting

### Updates Not Showing
- Ensure app is installed from Play Store (not sideloaded)
- Check that new version has higher versionCode
- Verify app is published to a testing track
- Check Google Play Console for update availability

### Update Failed
- Check device storage space
- Ensure stable internet connection
- Check logs for error codes

## Logs

Monitor update flow with logcat:
```
adb logcat | grep InAppUpdateManager
```

## Best Practices

1. **Don't force updates too aggressively** - Use flexible updates for minor releases
2. **Use immediate updates sparingly** - Only for critical bug fixes or security updates
3. **Test thoroughly** - Always test update flows before production release
4. **Monitor metrics** - Track update adoption rates in Play Console
5. **Handle edge cases** - App handles interrupted updates gracefully

## Additional Resources

- [Google Play In-App Updates Documentation](https://developer.android.com/guide/playcore/in-app-updates)
- [Testing In-App Updates](https://developer.android.com/guide/playcore/in-app-updates/test)
- [Update Priority Guidelines](https://developer.android.com/guide/playcore/in-app-updates#check-priority)
