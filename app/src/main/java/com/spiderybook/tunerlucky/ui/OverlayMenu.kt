package com.spiderybook.tunerlucky.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.StatsData
import com.spiderybook.tunerlucky.domain.managers.PerformanceManager
import com.spiderybook.tunerlucky.ui.state.OverlayState
import com.spiderybook.tunerlucky.ui.theme.AccentBlue

@Composable
fun OverlayMenu(
    stats: StatsData,
    onClose: () -> Unit,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    val performanceManager = remember { PerformanceManager() }
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = if (isExpanded) Modifier.fillMaxSize() else Modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (isExpanded) {
            // Click outside to close
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isExpanded = false
                            onExpandedChange(false)
                        }
                    )
            )

            // Prevent clicks inside panel from closing
            Box(modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )) {
                GameHubPanel(
                    stats = stats,
                    performanceManager = performanceManager,
                    onClose = {
                        isExpanded = false
                        onExpandedChange(false)
                    },
                    onExit = onClose
                )
            }
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
            .width(20.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -5) {
                        val now = System.currentTimeMillis()
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
    var currentTab by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .width(360.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
            .background(Color(0xFF0A0F1D).copy(alpha = 0.95f))
    ) {
        // Left Icon Strip (Tabs)
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(Color(0xFF131A2B)),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Minimize", tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            
            // Tab 0: Performance
            TabIcon(
                icon = Icons.Default.MonitorHeart,
                isSelected = currentTab == 0,
                onClick = { currentTab = 0 }
            )
            // Tab 1: Settings
            TabIcon(
                icon = Icons.Default.Settings,
                isSelected = currentTab == 1,
                onClick = { currentTab = 1 }
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }

        // Content Area
        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            when (currentTab) {
                0 -> PerformanceTab(stats, performanceManager)
                1 -> SettingsTab(performanceManager)
            }
            
            // Fixed Exit Button at top right
            Text(
                text = "EXIT",
                color = Color.Red.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onExit)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TabIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Gray)
    }
}

@Composable
private fun PerformanceTab(stats: StatsData, performanceManager: PerformanceManager) {
    val hudConfig by OverlayState.hudConfig.collectAsState()
    var isBoostEnabled by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Performance", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // HUD Display Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HUD Display", color = Color.White, fontSize = 16.sp)
                Switch(
                    checked = hudConfig.isEnabled,
                    onCheckedChange = { OverlayState.hudConfig.value = hudConfig.copy(isEnabled = it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentBlue)
                )
            }

            if (hudConfig.isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HudCheckbox("CPU", hudConfig.showCpu) { OverlayState.hudConfig.value = hudConfig.copy(showCpu = it) }
                        HudCheckbox("Graph", false) { /* Omitted as requested */ }
                        HudCheckbox("TMP", hudConfig.showTmp) { OverlayState.hudConfig.value = hudConfig.copy(showTmp = it) }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HudCheckbox("GPU", hudConfig.showGpu) { OverlayState.hudConfig.value = hudConfig.copy(showGpu = it) }
                        HudCheckbox("FPS", hudConfig.showFps) { OverlayState.hudConfig.value = hudConfig.copy(showFps = it) }
                        HudCheckbox("RAM", hudConfig.showRam) { OverlayState.hudConfig.value = hudConfig.copy(showRam = it) }
                    }
                }
            }
        }

        GameHubToggleRow(
            title = "Sustained Perf (Root+)",
            checked = isBoostEnabled,
            onCheckedChange = { 
                isBoostEnabled = it
                if (it) performanceManager.enableBoost() else performanceManager.disableBoost()
            }
        )
        
        // Simple stat overview at the bottom
        Spacer(modifier = Modifier.weight(1f))
        Text("Performance Metrics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("CPU: ${stats.cpuFreq}", color = Color.Gray, fontSize = 12.sp)
        Text("GPU: ${stats.gpuFreq}", color = Color.Gray, fontSize = 12.sp)
        Text("RAM: ${stats.ramUsed}", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsTab(performanceManager: PerformanceManager) {
    var isWifiOptEnabled by remember { mutableStateOf(false) }
    var isDndEnabled by remember { mutableStateOf(false) }
    var selectedHz by remember { mutableStateOf(60) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("System Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // Refresh Rate Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(16.dp)
        ) {
            Text("Display Refresh Rate", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HzButton("60Hz", selectedHz == 60, Modifier.weight(1f)) {
                    selectedHz = 60
                    performanceManager.set60Hz()
                }
                HzButton("120Hz", selectedHz == 120, Modifier.weight(1f)) {
                    selectedHz = 120
                    performanceManager.set120Hz()
                }
                HzButton("144Hz", selectedHz == 144, Modifier.weight(1f)) {
                    selectedHz = 144
                    performanceManager.set144Hz()
                }
            }
        }

        GameHubToggleRow(
            title = "WiFi Optimization",
            checked = isWifiOptEnabled,
            onCheckedChange = { 
                isWifiOptEnabled = it
                if (it) performanceManager.enableWifiGaming() else performanceManager.disableWifiGaming()
            }
        )

        GameHubToggleRow(
            title = "Do Not Disturb",
            checked = isDndEnabled,
            onCheckedChange = { 
                isDndEnabled = it
                if (it) performanceManager.enableDoNotDisturb() else performanceManager.disableDoNotDisturb()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { performanceManager.clearRam() }
                .background(AccentBlue.copy(alpha = 0.1f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Memory, contentDescription = null, tint = AccentBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Memory Instantly", color = AccentBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HzButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AccentBlue else Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HudCheckbox(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onCheckedChange(!isChecked) }) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
private fun GameHubToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = AccentBlue)
        )
    }
}
