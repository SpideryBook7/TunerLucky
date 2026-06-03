package com.spiderybook.tunerlucky.domain.managers

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.spiderybook.tunerlucky.data.StatsData
import com.spiderybook.tunerlucky.shizuku.ShizukuManager
import java.io.File
import kotlin.math.roundToInt

class StatsMonitor(
    private val context: Context
) {

    fun read(): StatsData =
        StatsData(
            cpuFreq = readCpuFreq(),
            gpuFreq = readGpuFreq(),
            ramUsed = readRam(),
            temperature = readTemperature(),
            battery = readBattery(),
            fps = readFps(),
            storage = readStorage()
        )

    private fun readCpuFreq(): String {
        val cpuRoot = File("/sys/devices/system/cpu")
        val freqs = cpuRoot.listFiles { file -> file.name.matches(Regex("cpu\\d+")) }
            ?.mapNotNull { File(it, "cpufreq/scaling_cur_freq").readLongOrNull() }
            .orEmpty()
        val maxKhz = freqs.maxOrNull() ?: return "N/A"
        return "%.2f GHz".format(maxKhz / 1_000_000.0)
    }

    private fun readGpuFreq(): String {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/kgsl/kgsl-3d0/cur_freq",
            "/sys/kernel/gpu/gpu_clock"
        )
        val hz = paths.firstNotNullOfOrNull { File(it).readLongOrNull() } ?: return "N/A"
        val mhz = if (hz > 1_000_000) hz / 1_000_000 else hz / 1_000
        return "$mhz MHz"
    }

    private fun readRam(): String {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        val used = info.totalMem - info.availMem
        return "${used.toGb()} / ${info.totalMem.toGb()}"
    }

    private fun readTemperature(): String {
        val zones = File("/sys/class/thermal").listFiles { file ->
            file.name.startsWith("thermal_zone")
        }.orEmpty()

        val temp = zones.firstNotNullOfOrNull { zone ->
            val type = File(zone, "type").readTextOrNull()?.lowercase().orEmpty()
            val raw = File(zone, "temp").readLongOrNull()
            if (raw != null && (type.contains("cpu") || type.contains("soc") || type.contains("skin"))) {
                raw
            } else {
                null
            }
        } ?: zones.firstNotNullOfOrNull { File(it, "temp").readLongOrNull() }

        val celsius = temp?.let { if (it > 1000) it / 1000.0 else it.toDouble() } ?: return "N/A"
        return "${celsius.roundToInt()} C"
    }

    private fun readBattery(): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return "N/A"
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return "N/A"
        return "${(level * 100f / scale).roundToInt()}%"
    }

    private fun readStorage(): String {
        val stats = StatFs(Environment.getDataDirectory().absolutePath)
        val total = stats.blockSizeLong * stats.blockCountLong
        val free = stats.blockSizeLong * stats.availableBlocksLong
        return "${(total - free).toGb()} / ${total.toGb()}"
    }

    private fun readFps(): String {
        val output = ShizukuManager.runCommand("dumpsys SurfaceFlinger --latency")
        if (output.startsWith("Service not connected") || output.startsWith("Error") || output.isBlank()) {
            return "N/A"
        }
        val lines = output.lines()
        val refreshPeriod = lines.firstOrNull()?.trim()?.toLongOrNull()
        if (refreshPeriod != null && refreshPeriod > 0) {
            val fps = (1_000_000_000.0 / refreshPeriod).roundToInt()
            return "$fps"
        }
        return "N/A"
    }

    private fun File.readLongOrNull(): Long? =
        runCatching { readText().trim().toLong() }.getOrNull()

    private fun File.readTextOrNull(): String? =
        runCatching { readText().trim() }.getOrNull()

    private fun Long.toGb(): String =
        "%.1f GB".format(this / 1024.0 / 1024.0 / 1024.0)
}
