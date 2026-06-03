package com.spiderybook.tunerlucky.shizuku

object SysFsReader {
    fun getCpuFreq(): String {
        // Read current frequency for CPU 0 (as proxy for cluster)
        val result = ShizukuManager.runCommand("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
        return try {
            val khz = result.trim().toLong()
            "${String.format("%.2f", khz / 1000000.0)} GHz"
        } catch (e: Exception) {
            "0.00 GHz"
        }
    }

    fun getGpuFreq(): String {
        // Paths vary by SoC, for Snapdragon 7s Gen 2 (Adreno 710)
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/kgsl/kgsl-3d0/cur_freq",
            "/sys/kernel/gpu/gpu_clock"
        )
        for (path in paths) {
            val result = ShizukuManager.runCommand("cat $path")
            if (result.isNotEmpty() && !result.contains("Error")) {
                return try {
                    val mhz = result.trim().toLong() / 1000000
                    "$mhz MHz"
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return "0 MHz"
    }
}
