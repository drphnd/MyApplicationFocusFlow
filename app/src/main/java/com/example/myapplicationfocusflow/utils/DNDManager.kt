package com.example.myapplicationfocusflow.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DND Manager untuk mengatur mode Do Not Disturb
 * Handles different Android versions and permissions
 */
class DNDManager(private val context: Context) {

    private val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: throw IllegalStateException("NotificationManager not available")
    }

    /**
     * Cek apakah app memiliki permission untuk mengatur DND
     */
    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true // Android 5.1 dan dibawah tidak perlu permission khusus
        }
    }

    /**
     * Buka settings untuk memberikan permission DND
     */
    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Enable DND mode
     */
    suspend fun enableDND(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasPermission()) {
                return@withContext false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0-5.1
                @Suppress("DEPRECATION")
                android.provider.Settings.Global.putInt(
                    context.contentResolver,
                    "zen_mode",
                    1 // 1 = Priority interruptions only, 2 = No interruptions
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Disable DND mode (restore normal mode)
     */
    suspend fun disableDND(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasPermission()) {
                return@withContext false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0-5.1
                @Suppress("DEPRECATION")
                android.provider.Settings.Global.putInt(
                    context.contentResolver,
                    "zen_mode",
                    0 // 0 = Off
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Cek apakah DND sedang aktif
     */
    fun isDNDActive(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                @Suppress("DEPRECATION")
                android.provider.Settings.Global.getInt(
                    context.contentResolver,
                    "zen_mode",
                    0
                ) != 0
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Enable DND dengan durasi tertentu (dalam menit)
     * Hanya tersedia di Android 6.0+
     */
    suspend fun enableDNDWithDuration(durationMinutes: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!hasPermission() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return@withContext false
            }

            // Set DND for specific duration
            val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Set automatic rule untuk durasi tertentu
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)

                // Note: Untuk auto-disable setelah durasi tertentu,
                // kita perlu menggunakan AlarmManager atau WorkManager
                // yang akan dihandle di FocusSessionView
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val DND_PERMISSION_REQUEST_CODE = 1001

        /**
         * Get DND status description
         */
        fun getDNDStatusDescription(context: Context): String {
            val manager = DNDManager(context)
            return when {
                !manager.hasPermission() -> "Permission required"
                manager.isDNDActive() -> "DND Active"
                else -> "DND Inactive"
            }
        }
    }
}