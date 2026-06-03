package com.spiderybook.tunerlucky.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.StatsData
import com.spiderybook.tunerlucky.domain.managers.PerformanceManager
import com.spiderybook.tunerlucky.domain.managers.StatsMonitor
import com.spiderybook.tunerlucky.ui.theme.AccentBlue
import com.spiderybook.tunerlucky.ui.theme.DangerRed
import com.spiderybook.tunerlucky.ui.theme.GlassOverlay
import com.spiderybook.tunerlucky.ui.theme.SurfaceCard
import com.spiderybook.tunerlucky.ui.theme.TextPrimary
import com.spiderybook.tunerlucky.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun OverlayMenu(
    onClose: () -> Unit,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val statsMonitor = remember { StatsMonitor(context) }
    val performanceManager = remember { PerformanceManager() }

    var stats by remember {
        mutableStateOf(
            StatsData(
                cpuFreq = "N/A",
                gpuFreq = "N/A",
                ramUsed = "N/A",
                temperature = "N/A",
                battery = "N/A",
                fps = "N/A"
            )
        )
    }

    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (isActive) {
            stats = statsMonitor.read()
            delay(1000)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isExpanded) {
            LeftWing(stats = stats)
            RightWing(
                onClose = { onClose() },
                onMinimize = {
                    isExpanded = false
                    onExpandedChange(false)
                },
                performanceManager = performanceManager
            )
        } else {
            FloatingGameSpaceButton(
                fps = stats.fps,
                onClick = {
                    isExpanded = true
                    onExpandedChange(true)
                }
            )
        }
    }
}

@Composable
private fun FloatingGameSpaceButton(fps: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -5) {
                        onClick()
                    }
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.6f))
        )
    }
}

@Composable
private fun LeftWing(
    stats: StatsData
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(170.dp)
            .padding(
                start = 12.dp,
                top = 24.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverlayStatCard("FPS", stats.fps)
        OverlayStatCard("CPU", stats.cpuFreq)
        OverlayStatCard("GPU", stats.gpuFreq)
        OverlayStatCard("RAM", stats.ramUsed)
        OverlayStatCard("TEMP", stats.temperature)
        OverlayStatCard("BAT", stats.battery)
    }
}

@Composable
private fun RightWing(
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    performanceManager: PerformanceManager
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(170.dp)
            .padding(
                end = 12.dp,
                top = 24.dp,
                bottom = 24.dp
            )
            .wrapContentWidth(Alignment.End),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        OverlayActionCard("BOOST", Icons.Default.Speed) { performanceManager.enableBoost() }
        OverlayActionCard("RAM", Icons.Default.Memory) { performanceManager.clearRam() }
        OverlayActionCard("WIFI", Icons.Default.NetworkWifi) { performanceManager.enableWifiGaming() }
        OverlayActionCard("PROFILE", Icons.Default.Tune) { performanceManager.applyProfile(com.spiderybook.tunerlucky.data.PerformanceProfile.BALANCED) }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(18.dp)
        ) {
            IconButton(onClick = onMinimize) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = TextPrimary
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = DangerRed),
            shape = RoundedCornerShape(18.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun OverlayStatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = GlassOverlay),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = AccentBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OverlayActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = GlassOverlay),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AccentBlue
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
