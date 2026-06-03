package com.spiderybook.tunerlucky.data

data class StatsData(

    val cpuFreq: String,

    val gpuFreq: String,

    val ramUsed: String,

    val temperature: String,

    val battery: String,

    val fps: String,

    val storage: String = "N/A"
)
