package com.spiderybook.tunerlucky.shizuku

import android.content.pm.PackageManager
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.spiderybook.tunerlucky.IShellService
import rikka.shizuku.Shizuku
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ShizukuManager {
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission

    private var shellService: IShellService? = null
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            shellService = IShellService.Stub.asInterface(binder)
            _isServiceConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            _isServiceConnected.value = false
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        _isReady.value = true
        checkPermission()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isReady.value = false
        _hasPermission.value = false
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == 1) {
            _hasPermission.value = grantResult == PackageManager.PERMISSION_GRANTED
        }
    }

    fun init() {
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        
        if (Shizuku.pingBinder()) {
            _isReady.value = true
            checkPermission()
        }
    }

    private fun bindService() {
        if (_isServiceConnected.value) return
        
        val args = Shizuku.UserServiceArgs(ComponentName("com.spiderybook.tunerlucky", ShellService::class.java.name))
            .daemon(false)
            .processNameSuffix("shell")
            .debuggable(true)
            .version(1)
        
        try {
            Shizuku.bindUserService(args, serviceConnection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun runCommand(command: String): String {
        return shellService?.runCommand(command) ?: "Service not connected"
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        if (_isServiceConnected.value) {
            // unbind
        }
    }

    fun checkPermission() {
        if (Shizuku.isPreV11()) {
            _hasPermission.value = false
            return
        }
        
        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _hasPermission.value = true
                bindService()
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                _hasPermission.value = false
            } else {
                Shizuku.requestPermission(1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _hasPermission.value = false
        }
    }
}
