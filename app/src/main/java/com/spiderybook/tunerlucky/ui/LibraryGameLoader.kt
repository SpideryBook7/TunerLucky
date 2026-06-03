package com.spiderybook.tunerlucky.data

import android.content.Context
import android.content.pm.PackageManager

object LibraryGameLoader {

    fun loadGames(
        context: Context,
        packages: List<String>,
        profiles: Map<String, PerformanceProfile> = emptyMap(),
        favorites: Set<String> = emptySet(),
        lastPlayed: Map<String, Long> = emptyMap()
    ): List<GameInfo> {

        val pm = context.packageManager

        return packages.mapNotNull { packageName ->

            try {

                val appInfo =
                    pm.getApplicationInfo(
                        packageName,
                        0
                    )

                GameInfo(
                    packageName = packageName,
                    name = pm.getApplicationLabel(appInfo).toString(),
                    profile = profiles[packageName] ?: PerformanceProfile.BALANCED,
                    favorite = favorites.contains(packageName),
                    lastPlayed = lastPlayed[packageName] ?: 0L,
                    isEmulator = packageName.isKnownGamingPackage()
                )

            } catch (_: Exception) {

                null
            }
        }
    }

    private fun String.isKnownGamingPackage(): Boolean {
        val lower = lowercase()
        return listOf(
            "minecraft",
            "yuzu",
            "sudachi",
            "dolphin",
            "ppsspp",
            "winlator",
            "lime3ds",
            "retroarch"
        ).any { lower.contains(it) }
    }
}
