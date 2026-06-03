package com.spiderybook.tunerlucky.data

data class GameInfo(
    val packageName: String,
    val name: String,
    val iconUri: String? = null,
    val bannerUri: String? = null,
    val profile: PerformanceProfile = PerformanceProfile.BALANCED,
    val favorite: Boolean = false,
    val lastPlayed: Long = 0L,
    val isEmulator: Boolean = false
)
