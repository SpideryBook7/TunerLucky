package com.spiderybook.tunerlucky.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.spiderybook.tunerlucky.data.LibraryManager
import com.spiderybook.tunerlucky.data.PerformanceProfile
import com.spiderybook.tunerlucky.domain.managers.PerformanceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameDetectionService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val performanceManager = PerformanceManager()
    private var lastPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        monitorForegroundApp()
    }

    private fun monitorForegroundApp() {
        val libraryManager = LibraryManager(this)
        scope.launch {
            while (isActive) {
                val foregroundPackage = currentForegroundPackage()
                val library = libraryManager.libraryApps.first().toSet()
                if (foregroundPackage != null && foregroundPackage != lastPackage && foregroundPackage in library) {
                    val profiles = libraryManager.gameProfiles.first()
                    val profile = profiles[foregroundPackage] ?: PerformanceProfile.BALANCED
                    performanceManager.applyProfile(profile)
                    libraryManager.markPlayed(foregroundPackage)
                    libraryManager.addLog("$foregroundPackage detectado en foreground")
                    startForegroundService(Intent(this@GameDetectionService, OverlayService::class.java))
                    lastPackage = foregroundPackage
                }
                delay(1000)
            }
        }
    }

    private fun currentForegroundPackage(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        return usageStatsManager
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 10_000, now)
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lucky Tuner Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lucky Tuner")
            .setContentText("Deteccion de juegos activa")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "lucky_tuner_detection"
        private const val NOTIFICATION_ID = 102
    }
}
