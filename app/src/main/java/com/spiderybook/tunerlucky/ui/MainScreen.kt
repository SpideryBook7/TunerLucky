package com.spiderybook.tunerlucky.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.Prefs
import com.spiderybook.tunerlucky.shizuku.ShizukuManager
import com.spiderybook.tunerlucky.ui.theme.PrimaryNeon
import com.spiderybook.tunerlucky.ui.theme.SecondaryNeon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Gaming", "Energy", "Network")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LUCKY TUNER", fontWeight = FontWeight.Bold, color = PrimaryNeon) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.Black, contentColor = PrimaryNeon) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 10.sp) }
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardModule()
                1 -> GamingModule()
                2 -> EnergyModule()
                3 -> NetworkModule(prefs)
            }
        }
    }
}

@Composable
fun DashboardModule() {
    val isReady by ShizukuManager.isReady.collectAsState()
    val hasPermission by ShizukuManager.hasPermission.collectAsState()
    val isConnected by ShizukuManager.isServiceConnected.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        StatusCard("Shizuku Status", if (isReady) "Running" else "Not Running", if (isReady) PrimaryNeon else Color.Red)
        StatusCard("Permission", if (hasPermission) "Granted" else "Denied", if (hasPermission) PrimaryNeon else Color.Red)
        StatusCard("Shell Service", if (isConnected) "Connected" else "Disconnected", if (isConnected) PrimaryNeon else Color.Red)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Device: Meizu Lucky 08", color = Color.White, fontSize = 14.sp)
        Text("CPU: Snapdragon 7s Gen 2", color = Color.White, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("AI Button (Power Button)", color = PrimaryNeon, fontWeight = FontWeight.Bold)
        Button(
            onClick = { ShizukuManager.runCommand("settings put secure double_tap_power_button_action 0") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Reset Double-Tap to None")
        }
    }
}

@Composable
fun StatusCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.Gray, modifier = Modifier.weight(1f))
            Text(value, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GamingModule() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Performance Optimization", color = PrimaryNeon, fontWeight = FontWeight.Bold)
        Button(
            onClick = { ShizukuManager.runCommand("settings put peak_refresh_rate 144.0") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeon)
        ) {
            Text("Force 144Hz Refresh Rate")
        }
        
        Button(
            onClick = { ShizukuManager.runCommand("am force-stop com.meizu.mstore") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Clear RAM (Force Stop Store)")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Driver Vault", color = PrimaryNeon, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Turnip Drivers", color = Color.White, fontWeight = FontWeight.Bold)
                Text("V24.1.0 R18 - Installed", color = PrimaryNeon, fontSize = 12.sp)
                Button(onClick = { /* Download */ }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Check for Updates")
                }
            }
        }
    }
}

@Composable
fun EnergyModule() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("App Battery Control", color = PrimaryNeon, fontWeight = FontWeight.Bold)
        Text("Restrict background activities for heavy apps.", color = Color.LightGray, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val apps = listOf("com.google.android.youtube", "com.facebook.katana", "com.instagram.android")
        apps.forEach { pkg ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(pkg, color = Color.White, modifier = Modifier.weight(1f), fontSize = 12.sp)
                Button(onClick = { 
                    ShizukuManager.runCommand("appops set $pkg WAKE_LOCK ignore")
                    ShizukuManager.runCommand("appops set $pkg RUN_IN_BACKGROUND ignore")
                }) {
                    Text("Restrict")
                }
            }
        }
    }
}

@Composable
fun NetworkModule(prefs: Prefs) {
    val coroutineScope = rememberCoroutineScope()
    val wifiFixEnabled by prefs.wifiOptimization.collectAsState(initial = false)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("WiFi Stability Fix", color = PrimaryNeon, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Disable WiFi Suspend", color = Color.White, modifier = Modifier.weight(1f))
            Switch(
                checked = wifiFixEnabled,
                onCheckedChange = { enabled ->
                    coroutineScope.launch {
                        prefs.setWifiOptimization(enabled)
                        val value = if (enabled) "0" else "1"
                        ShizukuManager.runCommand("settings put global wifi_suspend_optimizations_enabled $value")
                    }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryNeon)
            )
        }
        Text("Prevents WiFi from dropping during sleep or gaming.", color = Color.Gray, fontSize = 12.sp)
    }
}
