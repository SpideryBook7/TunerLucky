package com.spiderybook.tunerlucky.ui.state

import kotlinx.coroutines.flow.MutableStateFlow

data class HudConfig(
    val isEnabled: Boolean = false,
    val showCpu: Boolean = true,
    val showGpu: Boolean = true,
    val showRam: Boolean = true,
    val showFps: Boolean = true,
    val showTmp: Boolean = true,
    val showBat: Boolean = true
)

object OverlayState {
    val hudConfig = MutableStateFlow(HudConfig())
}
