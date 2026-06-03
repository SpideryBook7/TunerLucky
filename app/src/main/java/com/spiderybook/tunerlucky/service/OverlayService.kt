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
import com.spiderybook.tunerlucky.ui.OverlayMenu

class OverlayService :
    Service(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager

    private var overlayView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private val lifecycleRegistry =
        LifecycleRegistry(this)

    private val viewModelStoreInternal =
        ViewModelStore()

    private val savedStateController =
        SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = viewModelStoreInternal

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    override fun onCreate() {

        super.onCreate()

        savedStateController.performRestore(null)

        lifecycleRegistry.handleLifecycleEvent(
            Lifecycle.Event.ON_CREATE
        )

        lifecycleRegistry.handleLifecycleEvent(
            Lifecycle.Event.ON_START
        )

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        showOverlay()
    }

    private fun showOverlay() {

        windowManager =
            getSystemService(WINDOW_SERVICE)
                    as WindowManager

        val flags =
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            flags,
            PixelFormat.TRANSLUCENT
        )

        params?.gravity = Gravity.TOP or Gravity.START

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setContent {
                OverlayMenu(
                    onClose = { stopSelf() },
                    onExpandedChange = { isExpanded -> updateWindowSize(isExpanded) }
                )
            }
        }

        overlayView = composeView
        windowManager.addView(overlayView, params)
    }

    private fun updateWindowSize(isExpanded: Boolean) {
        if (params == null || overlayView == null) return
        if (isExpanded) {
            params?.width = WindowManager.LayoutParams.MATCH_PARENT
            params?.height = WindowManager.LayoutParams.MATCH_PARENT
        } else {
            params?.width = WindowManager.LayoutParams.WRAP_CONTENT
            params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        windowManager.updateViewLayout(overlayView, params)
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Lucky Tuner Overlay",
                    NotificationManager.IMPORTANCE_LOW
                )

            channel.description =
                "Overlay de Game Space"

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle(
                "Lucky Tuner"
            )
            .setContentText(
                "Overlay activo"
            )
            .setSmallIcon(
                android.R.drawable.ic_dialog_info
            )
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {

        overlayView?.let {

            try {

                windowManager.removeView(it)

            } catch (_: Exception) {
            }
        }

        lifecycleRegistry.handleLifecycleEvent(
            Lifecycle.Event.ON_STOP
        )

        lifecycleRegistry.handleLifecycleEvent(
            Lifecycle.Event.ON_DESTROY
        )

        viewModelStoreInternal.clear()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    companion object {

        private const val CHANNEL_ID =
            "lucky_tuner_overlay"

        private const val NOTIFICATION_ID =
            101
    }
}