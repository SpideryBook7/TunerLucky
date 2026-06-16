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
import com.spiderybook.tunerlucky.ui.state.OverlayState
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
    private var isHudAdded = false

    // Game package tracker
    private var gamePackage: String? = null

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
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        statsMonitor = StatsMonitor(this)
        
        // Start stats polling centrally
        serviceScope.launch {
            while (isActive) {
                statsFlow.value = statsMonitor.read()
                delay(1000)
            }
        }

        // Initialize HUD config from persistent storage
        val libraryManager = com.spiderybook.tunerlucky.data.LibraryManager(this)
        serviceScope.launch {
            libraryManager.fpsCounter.collect { enabled ->
                android.util.Log.d("OverlayService", "fpsCounter collected from DB: $enabled")
                OverlayState.hudConfig.value = OverlayState.hudConfig.value.copy(isEnabled = enabled)
            }
        }

        // Auto-close monitor
        serviceScope.launch {
            // Give the game time to launch before we start checking
            delay(5000)
            while (isActive) {
                try {
                    val focus = com.spiderybook.tunerlucky.shizuku.ShizukuManager.runCommand("dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'")
                    if (focus.isNotBlank() && !focus.startsWith("Service not connected")) {
                        // Check if focused application shifted away
                        val isGameFocused = gamePackage?.let { focus.contains(it) } ?: false
                        val isLauncherFocused = focus.contains("com.spiderybook.tunerlucky")
                        val isSystemOrImeFocused = focus.contains("systemui", ignoreCase = true) ||
                                focus.contains("inputmethod", ignoreCase = true) ||
                                focus.contains("permissioncontroller", ignoreCase = true) ||
                                focus.contains("android", ignoreCase = true)
                        val isGeneralLauncherFocused = focus.contains("launcher", ignoreCase = true) || focus.contains("nexuslauncher", ignoreCase = true)
                        
                        if (!isGameFocused && !isLauncherFocused && !isSystemOrImeFocused && !isGeneralLauncherFocused) {
                            // Wait to ensure it's not a transient state (like opening a notification panel)
                            delay(2000)
                            val checkAgain = com.spiderybook.tunerlucky.shizuku.ShizukuManager.runCommand("dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'")
                            val isGameFocusedAgain = gamePackage?.let { checkAgain.contains(it) } ?: false
                            val isLauncherFocusedAgain = checkAgain.contains("com.spiderybook.tunerlucky")
                            val isSystemOrImeFocusedAgain = checkAgain.contains("systemui", ignoreCase = true) ||
                                    checkAgain.contains("inputmethod", ignoreCase = true) ||
                                    checkAgain.contains("permissioncontroller", ignoreCase = true) ||
                                    checkAgain.contains("android", ignoreCase = true)
                            val isGeneralLauncherFocusedAgain = checkAgain.contains("launcher", ignoreCase = true) || checkAgain.contains("nexuslauncher", ignoreCase = true)
                            
                            if (!isGameFocusedAgain && !isLauncherFocusedAgain && !isSystemOrImeFocusedAgain && !isGeneralLauncherFocusedAgain) {
                                try {
                                    com.spiderybook.tunerlucky.shizuku.ShizukuManager.runCommand("settings put global policy_control null")
                                } catch (_: Exception) {}
                                stopSelf()
                                break
                            }
                        }
                    }
                } catch (e: Exception) {}
                delay(3000)
            }
        }

        showSidebar()
        showHud()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("game_package")?.let {
            gamePackage = it
        }
        return START_STICKY
    }

    private fun showSidebar() {
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

        // Observe config to add/remove HUD view dynamically
        serviceScope.launch(Dispatchers.Main) {
            OverlayState.hudConfig.collect { config ->
                android.util.Log.d("OverlayService", "hudConfig collected: isEnabled=${config.isEnabled}, isHudAdded=$isHudAdded")
                if (hudView != null && hudParams != null) {
                    if (config.isEnabled) {
                        if (!isHudAdded) {
                            try {
                                windowManager.addView(hudView, hudParams)
                                isHudAdded = true
                                android.util.Log.d("OverlayService", "HUD view successfully added to WindowManager")
                            } catch (e: Exception) {
                                android.util.Log.e("OverlayService", "Failed to add HUD view", e)
                            }
                        }
                    } else {
                        if (isHudAdded) {
                            try {
                                windowManager.removeView(hudView)
                                isHudAdded = false
                                android.util.Log.d("OverlayService", "HUD view successfully removed from WindowManager")
                            } catch (e: Exception) {
                                android.util.Log.e("OverlayService", "Failed to remove HUD view", e)
                            }
                        }
                    }
                }
            }
        }
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
        if (isHudAdded && hudView != null) {
            try { windowManager.removeView(hudView) } catch (_: Exception) {}
            isHudAdded = false
        }
        
        try {
            com.spiderybook.tunerlucky.shizuku.ShizukuManager.runCommand("settings put global policy_control null")
        } catch (_: Exception) {}

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