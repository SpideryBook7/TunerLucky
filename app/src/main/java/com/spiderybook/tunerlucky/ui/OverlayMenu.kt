package com.spiderybook.tunerlucky.ui

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings as IconSettings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.PerformanceProfile
import com.spiderybook.tunerlucky.data.StatsData
import com.spiderybook.tunerlucky.domain.managers.PerformanceManager
import com.spiderybook.tunerlucky.ui.state.OverlayState
import com.spiderybook.tunerlucky.ui.theme.AccentBlue
import com.spiderybook.tunerlucky.shizuku.ShizukuManager
import kotlinx.coroutines.launch

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
            .width(8.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF4F7BFF).copy(alpha = 0.3f),
                        Color(0xFF9A57FF).copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF4F7BFF).copy(alpha = 0.4f),
                        Color(0xFF9A57FF).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -3) {
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
                .width(1.5.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color.White.copy(alpha = 0.7f))
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
    val shizukuConnected by ShizukuManager.isServiceConnected.collectAsState()

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
            
            // Tab 0: Performance & Stats
            TabIcon(
                icon = Icons.Default.MonitorHeart,
                isSelected = currentTab == 0,
                onClick = { currentTab = 0 }
            )
            // Tab 1: System Settings
            TabIcon(
                icon = Icons.Default.Settings,
                isSelected = currentTab == 1,
                onClick = { currentTab = 1 }
            )
            // Tab 2: Smart Profiles
            TabIcon(
                icon = Icons.Default.Analytics,
                isSelected = currentTab == 2,
                onClick = { currentTab = 2 }
            )
            // Tab 3: Frame Generation (LSFG)
            TabIcon(
                icon = Icons.Default.Layers,
                isSelected = currentTab == 3,
                onClick = { currentTab = 3 }
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }

        // Content Area
        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Shizuku Connection Warning Banner if not connected
                if (!shizukuConnected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF8B0000).copy(alpha = 0.20f))
                            .border(1.dp, Color(0xFFFF375F).copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF375F),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Shizuku desconectado. Optimización de hardware limitada.",
                            color = Color(0xFFFFC0C0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        0 -> PerformanceTab(stats)
                        1 -> SettingsTab(stats, performanceManager)
                        2 -> ProfilesTab(stats, performanceManager)
                        3 -> LSFGTab()
                    }
                }
            }
            
            // Fixed Exit Button at top right
            Text(
                text = "SALIR",
                color = Color.Red.copy(alpha = 0.9f),
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
private fun TabIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
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
private fun PerformanceTab(stats: StatsData) {
    val hudConfig by OverlayState.hudConfig.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val libraryManager = remember { com.spiderybook.tunerlucky.data.LibraryManager(context) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Rendimiento", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // The stats display cards
        GameHubStatRow("FPS", stats.fps, "CPU", stats.cpuFreq)
        GameHubStatRow("GPU", stats.gpuFreq, "RAM", stats.ramUsed)
        GameHubStatRow("TMP", stats.temperature, "BAT", stats.battery)

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
                Text("Mostrar HUD en Juego", color = Color.White, fontSize = 16.sp)
                Switch(
                    checked = hudConfig.isEnabled,
                    onCheckedChange = { isEnabled ->
                        OverlayState.hudConfig.value = hudConfig.copy(isEnabled = isEnabled)
                        scope.launch { libraryManager.setFpsCounter(isEnabled) }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentBlue)
                )
            }

            if (hudConfig.isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HudCheckbox("CPU", hudConfig.showCpu) { OverlayState.hudConfig.value = hudConfig.copy(showCpu = it) }
                        HudCheckbox("TMP", hudConfig.showTmp) { OverlayState.hudConfig.value = hudConfig.copy(showTmp = it) }
                        HudCheckbox("BAT", hudConfig.showBat) { OverlayState.hudConfig.value = hudConfig.copy(showBat = it) }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HudCheckbox("GPU", hudConfig.showGpu) { OverlayState.hudConfig.value = hudConfig.copy(showGpu = it) }
                        HudCheckbox("FPS", hudConfig.showFps) { OverlayState.hudConfig.value = hudConfig.copy(showFps = it) }
                        HudCheckbox("RAM", hudConfig.showRam) { OverlayState.hudConfig.value = hudConfig.copy(showRam = it) }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsTab(stats: StatsData, performanceManager: PerformanceManager) {
    val context = LocalContext.current
    
    // Read current refresh rate from display
    val currentHz = remember(stats) {
        runCatching {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else {
                wm.defaultDisplay
            }
            display?.refreshRate?.toInt() ?: 60
        }.getOrDefault(60)
    }

    // Read current WiFi Gaming mode (suspend optimizations == 0)
    val isWifiOptEnabled = remember(stats) {
        Settings.Global.getInt(context.contentResolver, "wifi_suspend_optimizations_enabled", 1) == 0
    }

    // Read current DND state
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    val isDndActive = remember(stats) {
        notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ajustes de Sistema", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // Refresh Rate Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(16.dp)
        ) {
            Text("Tasa de Refresco de Pantalla", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HzButton("60Hz", currentHz == 60, Modifier.weight(1f)) {
                    performanceManager.set60Hz()
                }
                HzButton("90Hz", currentHz == 90, Modifier.weight(1f)) {
                    performanceManager.set90Hz()
                }
                HzButton("120Hz", currentHz >= 115 && currentHz <= 125, Modifier.weight(1f)) {
                    performanceManager.set120Hz()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HzButton("144Hz Extremo", currentHz >= 140, Modifier.fillMaxWidth()) {
                performanceManager.set144Hz()
            }
        }

        GameHubToggleRow(
            title = "Optimización de WiFi",
            checked = isWifiOptEnabled,
            onCheckedChange = { enable ->
                if (enable) performanceManager.enableWifiGaming() else performanceManager.disableWifiGaming()
            }
        )

        GameHubToggleRow(
            title = "Modo No Molestar (DND)",
            checked = isDndActive,
            onCheckedChange = { enable ->
                if (enable) performanceManager.enableDoNotDisturb() else performanceManager.disableDoNotDisturb()
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
            Text("Liberar Memoria RAM Instante", color = AccentBlue, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfilesTab(stats: StatsData, performanceManager: PerformanceManager) {
    val context = LocalContext.current
    
    // Read Hz and WiFi settings to determine active profile
    val currentHz = remember(stats) {
        runCatching {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display
            } else {
                wm.defaultDisplay
            }
            display?.refreshRate?.toInt() ?: 60
        }.getOrDefault(60)
    }

    val isWifiOptEnabled = remember(stats) {
        Settings.Global.getInt(context.contentResolver, "wifi_suspend_optimizations_enabled", 1) == 0
    }

    val activeProfile = remember(currentHz, isWifiOptEnabled) {
        when {
            currentHz == 60 -> PerformanceProfile.BATTERY
            currentHz >= 140 && isWifiOptEnabled -> PerformanceProfile.EXTREME
            currentHz >= 115 -> PerformanceProfile.PERFORMANCE
            else -> PerformanceProfile.BALANCED
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Perfiles Inteligentes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Aplica configuraciones completas de hardware al instante para optimizar tu sesión.", color = Color.Gray, fontSize = 12.sp)

        GameHubActionRow(
            title = "Ahorro de Batería",
            subtitle = "60Hz, Rendimiento Económico",
            icon = Icons.Default.Tune,
            isActive = activeProfile == PerformanceProfile.BATTERY,
            onClick = { performanceManager.applyProfile(PerformanceProfile.BATTERY) }
        )
        GameHubActionRow(
            title = "Equilibrado",
            subtitle = "120Hz, Optimización Estándar",
            icon = Icons.Default.Tune,
            isActive = activeProfile == PerformanceProfile.BALANCED,
            onClick = { performanceManager.applyProfile(PerformanceProfile.BALANCED) }
        )
        GameHubActionRow(
            title = "Rendimiento",
            subtitle = "120Hz, Modo Fijo de CPU Activo",
            icon = Icons.Default.Speed,
            isActive = activeProfile == PerformanceProfile.PERFORMANCE,
            onClick = { performanceManager.applyProfile(PerformanceProfile.PERFORMANCE) }
        )
        GameHubActionRow(
            title = "Gaming Extremo",
            subtitle = "144Hz, Optimización WiFi y CPU al Máximo",
            icon = Icons.Default.Memory,
            isActive = activeProfile == PerformanceProfile.EXTREME,
            onClick = { performanceManager.applyProfile(PerformanceProfile.EXTREME) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
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
        modifier = modifier.height(60.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.Gray, fontSize = 10.sp)
            Text(value, color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun GameHubActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (isActive) AccentBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (isActive) AccentBlue else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isActive) AccentBlue else Color.White,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        if (isActive) {
            Text(
                "ACTIVO",
                color = AccentBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LSFGTab() {
    var isLsfgEnabled by remember { mutableStateOf(false) }
    var performanceMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Frame Generation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Lossless Scaling Frame Generation (LSFG). Interpolate frames to double the smoothness.", color = Color.Gray, fontSize = 12.sp)

        GameHubToggleRow(
            title = "Enable LSFG (Beta)",
            checked = isLsfgEnabled,
            onCheckedChange = { isLsfgEnabled = it }
        )

        if (isLsfgEnabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp)
            ) {
                Text("Optical Flow Mode", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HzButton("Performance", performanceMode, Modifier.weight(1f)) {
                        performanceMode = true
                    }
                    HzButton("Quality", !performanceMode, Modifier.weight(1f)) {
                        performanceMode = false
                    }
                }
            }
            
            GameHubActionRow(
                title = "Vulkan Compute API",
                subtitle = "Engine: Ready",
                icon = Icons.Default.Layers,
                isActive = false,
                onClick = {}
            )
            
            Text("Note: This feature is currently in architectural preview. The NDK interpolation engine will be connected in a future update.", color = AccentBlue, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
