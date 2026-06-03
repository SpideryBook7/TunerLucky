package com.spiderybook.tunerlucky.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.spiderybook.tunerlucky.domain.managers.StatsMonitor
import com.spiderybook.tunerlucky.ui.HudOverlay
import com.spiderybook.tunerlucky.ui.OverlayMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

class OverlayService :
    Service(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var statsMonitor: StatsMonitor

    // Sidebar View
    private var sidebarView: View? = null
    private var sidebarParams: WindowManager.LayoutParams? = null

    // HUD View
    private var hudView: View? = null
    private var hudParams: WindowManager.LayoutParams? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val viewModelStoreInternal = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    val statsFlow = MutableStateFlow(com.spiderybook.tunerlucky.data.StatsData(
        cpuFreq = "N/A", gpuFreq = "N/A", ramUsed = "N/A",
        temperature = "N/A", battery = "N/A", fps = "N/A"
    ))

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = viewModelStoreInternal

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        statsMonitor = StatsMonitor(this)
        
        // Start stats polling centrally
        serviceScope.launch {
            while (isActive) {
                statsFlow.value = statsMonitor.read()
                delay(1000)
            }
        }

        showSidebar()
        showHud()
    }

    private fun showSidebar() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        sidebarParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            flags,
            PixelFormat.TRANSLUCENT
        )

        sidebarParams?.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setContent {
                val stats by statsFlow.collectAsState()
                OverlayMenu(
                    stats = stats,
                    onClose = { stopSelf() },
                    onExpandedChange = { isExpanded -> updateSidebarSize(isExpanded) }
                )
            }
        }

        sidebarView = composeView
        windowManager.addView(sidebarView, sidebarParams)
    }

    private fun showHud() {
        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE // CRITICAL: Don't block game touches

        hudParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            flags,
            PixelFormat.TRANSLUCENT
        )

        hudParams?.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setContent {
                val stats by statsFlow.collectAsState()
                HudOverlay(stats = stats)
            }
        }

        hudView = composeView
        windowManager.addView(hudView, hudParams)
    }

    private fun updateSidebarSize(isExpanded: Boolean) {
        if (sidebarParams == null || sidebarView == null) return
        if (isExpanded) {
            sidebarParams?.width = WindowManager.LayoutParams.MATCH_PARENT
            sidebarParams?.height = WindowManager.LayoutParams.MATCH_PARENT
        } else {
            sidebarParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
            sidebarParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        windowManager.updateViewLayout(sidebarView, sidebarParams)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lucky Tuner Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Overlay de Game Space"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lucky Tuner")
            .setContentText("Overlay activo")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        sidebarView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        hudView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStoreInternal.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "lucky_tuner_overlay"
        private const val NOTIFICATION_ID = 101
    }
}