package com.spiderybook.tunerlucky.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.PerformanceProfile
import com.spiderybook.tunerlucky.data.StatsData
import com.spiderybook.tunerlucky.domain.managers.PerformanceManager
import com.spiderybook.tunerlucky.domain.managers.StatsMonitor
import com.spiderybook.tunerlucky.ui.theme.AccentBlue
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
                cpuFreq = "N/A", gpuFreq = "N/A", ramUsed = "N/A",
                temperature = "N/A", battery = "N/A", fps = "N/A"
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
        modifier = if (isExpanded) Modifier.fillMaxHeight().wrapContentWidth() else Modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (isExpanded) {
            GameHubPanel(
                stats = stats,
                performanceManager = performanceManager,
                onClose = {
                    isExpanded = false
                    onExpandedChange(false)
                },
                onExit = onClose
            )
        } else {
            EdgeSwipeTrigger(
                onClick = {
                    isExpanded = true
                    onExpandedChange(true)
                }
            )
        }
    }
}

@Composable
private fun EdgeSwipeTrigger(onClick: () -> Unit) {
    var lastSwipeTime by remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .width(16.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -5) {
                        val now = System.currentTimeMillis()
                        // Double swipe within 1000ms triggers the overlay
                        if (now - lastSwipeTime < 1000) {
                            onClick()
                            lastSwipeTime = 0L
                        } else {
                            lastSwipeTime = now
                        }
                    }
                }
            }
            .clickable {
                val now = System.currentTimeMillis()
                if (now - lastSwipeTime < 1000) {
                    onClick()
                    lastSwipeTime = 0L
                } else {
                    lastSwipeTime = now
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color.White.copy(alpha = 0.4f))
        )
    }
}

@Composable
private fun GameHubPanel(
    stats: StatsData,
    performanceManager: PerformanceManager,
    onClose: () -> Unit,
    onExit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .width(360.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.95f))
            .padding(16.dp)
    ) {
        // Left Icon Strip
        Column(
            modifier = Modifier.width(60.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Minimize", tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { performanceManager.enableBoost() }) {
                Icon(Icons.Default.Speed, contentDescription = "Boost", tint = AccentBlue)
            }
            IconButton(onClick = { performanceManager.clearRam() }) {
                Icon(Icons.Default.Memory, contentDescription = "RAM", tint = AccentBlue)
            }
            IconButton(onClick = { performanceManager.applyProfile(PerformanceProfile.PERFORMANCE) }) {
                Icon(Icons.Default.Tune, contentDescription = "Profile", tint = AccentBlue)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Right Settings Area
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Performance", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    text = "EXIT",
                    color = Color.Red.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onExit)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            GameHubStatRow("FPS", stats.fps, "CPU", stats.cpuFreq)
            GameHubStatRow("GPU", stats.gpuFreq, "RAM", stats.ramUsed)
            GameHubStatRow("TEMP", stats.temperature, "BAT", stats.battery)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Quick Actions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            GameHubActionRow(
                "Performance Boost",
                "Maximize CPU/GPU clocks",
                Icons.Default.Speed,
                onClick = { performanceManager.enableBoost() }
            )
            GameHubActionRow(
                "Clear Memory",
                "Free up RAM for game",
                Icons.Default.Memory,
                onClick = { performanceManager.clearRam() }
            )
            GameHubActionRow(
                "Network Optimize",
                "Reduce WiFi latency",
                Icons.Default.NetworkWifi,
                onClick = { performanceManager.enableWifiGaming() }
            )
        }
    }
}

@Composable
private fun GameHubStatRow(title1: String, val1: String, title2: String, val2: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GameHubStatCard(modifier = Modifier.weight(1f), title1, val1)
        GameHubStatCard(modifier = Modifier.weight(1f), title2, val2)
    }
}

@Composable
private fun GameHubStatCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(
        modifier = modifier.height(70.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GameHubActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
