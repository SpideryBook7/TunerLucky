package com.spiderybook.tunerlucky.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF030D22).copy(alpha = 0.85f))
            .border(1.dp, Color(0xFF4F7BFF).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (config.showFps) {
            HudMetric("FPS", stats.fps, Color(0xFF34C759))
        }
        
        if (config.showCpu) {
            HudSeparator()
            HudMetric("CPU", stats.cpuFreq, Color(0xFFFFB000))
        }

        if (config.showGpu) {
            HudSeparator()
            // Map raw frequency into an estimated display percentage or display raw MHz
            val gpuDisplay = stats.gpuFreq.replace(" MHz", "M")
            HudMetric("GPU", gpuDisplay, Color(0xFF57FF74))
        }
        
        if (config.showRam) {
            HudSeparator()
            HudMetric("RAM", stats.ramUsed, Color(0xFF00CFFF))
        }
        
        if (config.showBat) {
            HudSeparator()
            HudMetric("BAT", stats.battery, Color(0xFFBF5AF2))
        }
        
        if (config.showTmp) {
            HudSeparator()
            HudMetric("TMP", stats.temperature, Color(0xFFFF375F))
        }
    }
}

@Composable
private fun HudMetric(label: String, value: String, accentColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Neon status indicator dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        if (value.isNotEmpty() && value != "N/A") {
            Text(
                text = value,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun HudSeparator() {
    Box(
        modifier = Modifier
            .size(1.dp, 8.dp)
            .background(Color.White.copy(alpha = 0.2f))
    )
}
