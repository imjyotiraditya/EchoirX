package app.echoirx.data.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val requiredPermissions = when {
        // Android 11+ (API 30+)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        // Android 10 (API 29) and Android 11 (API 30)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        // Android 9 (API 28) and below
        else -> {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun arePermissionsGranted(): Boolean = requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns an intent to request all-files access permission on Android 11+
     */
    fun getAllFilesAccessIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = "package:${context.packageName}".toUri()
            }
        } else {
            null
        }
    }

    // Media Permission Methods
    fun hasNotificationListenerPermission(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledListeners.contains(context.packageName)
    }

    fun getNotificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
}