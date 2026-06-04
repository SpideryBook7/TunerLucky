package com.spiderybook.tunerlucky.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.StatsData
import com.spiderybook.tunerlucky.ui.state.OverlayState

@Composable
fun HudOverlay(stats: StatsData) {
    val config by OverlayState.hudConfig.collectAsState()

    if (!config.isEnabled) return

    Row(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (config.showGpu) {
            HudMetric("GPU", stats.gpuFreq.replace(" MHz", "%"), Color(0xFF4CAF50)) // Simplified mapping
        }
        
        if (config.showCpu) {
            HudSeparator()
            HudMetric("CPU", stats.cpuFreq, Color(0xFFFFC107))
        }
        
        if (config.showRam) {
            HudSeparator()
            HudMetric("RAM", stats.ramUsed, Color(0xFF2196F3))
        }
        
        if (config.showBat) {
            HudSeparator()
            HudMetric("BAT", stats.battery, Color(0xFF9C27B0))
        }
        
        if (config.showTmp) {
            HudSeparator()
            HudMetric("TMP", stats.temperature, Color(0xFFF44336))
        }
        
        if (config.showFps) {
            HudSeparator()
            HudMetric("FPS", stats.fps, Color(0xFFCDDC39))
        }
    }
}

@Composable
private fun HudMetric(label: String, value: String, labelColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = labelColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        if (value.isNotEmpty()) {
            Text(value, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HudSeparator() {
    Text("|", color = Color.Gray, fontSize = 8.sp)
}
