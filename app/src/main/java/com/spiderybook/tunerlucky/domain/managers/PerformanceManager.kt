package com.spiderybook.tunerlucky.domain.managers

import com.spiderybook.tunerlucky.data.PerformanceProfile
import com.spiderybook.tunerlucky.shizuku.ShizukuManager

class PerformanceManager {

    fun applyProfile(profile: PerformanceProfile) {
        when (profile) {
            PerformanceProfile.BATTERY -> {
                set60Hz()
                disableBoost()
            }

            PerformanceProfile.BALANCED -> {
                set120Hz()
                disableBoost()
            }

            PerformanceProfile.PERFORMANCE -> {
                set120Hz()
                enableBoost()
            }

            PerformanceProfile.EXTREME -> {
                set144Hz()
                enableBoost()
                clearRam()
                enableWifiGaming()
            }
        }
    }

    fun enableBoost(): String =
        ShizukuManager.runCommand("cmd power set-fixed-performance-mode-enabled true")

    fun disableBoost(): String =
        ShizukuManager.runCommand("cmd power set-fixed-performance-mode-enabled false")

    fun clearRam(): String =
        ShizukuManager.runCommand("am kill-all")

    fun set60Hz(): String = setRefreshRate("60.0")

    fun set90Hz(): String = setRefreshRate("90.0")

    fun set120Hz(): String = setRefreshRate("120.0")

    fun set144Hz(): String = setRefreshRate("144.0")

    fun enableWifiGaming(): String =
        ShizukuManager.runCommand("settings put global wifi_suspend_optimizations_enabled 0")

    fun disableWifiGaming(): String =
        ShizukuManager.runCommand("settings put global wifi_suspend_optimizations_enabled 1")

    fun enableDoNotDisturb(): String =
        ShizukuManager.runCommand("cmd notification set_dnd priority")

    fun disableDoNotDisturb(): String =
        ShizukuManager.runCommand("cmd notification set_dnd off")

    private fun setRefreshRate(value: String): String {
        val min = ShizukuManager.runCommand("settings put system min_refresh_rate $value")
        val peak = ShizukuManager.runCommand("settings put system peak_refresh_rate $value")
        return listOf(min, peak).filter { it.isNotBlank() }.joinToString("\n")
    }
}
