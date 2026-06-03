package com.spiderybook.tunerlucky.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.GameInfo
import com.spiderybook.tunerlucky.data.LibraryGameLoader
import com.spiderybook.tunerlucky.data.LibraryManager
import com.spiderybook.tunerlucky.data.PerformanceProfile
import com.spiderybook.tunerlucky.data.StatsData
import com.spiderybook.tunerlucky.domain.managers.PerformanceManager
import com.spiderybook.tunerlucky.domain.managers.StatsMonitor
import com.spiderybook.tunerlucky.service.OverlayService
import com.spiderybook.tunerlucky.shizuku.ShizukuManager
import com.spiderybook.tunerlucky.ui.theme.AccentBlue
import com.spiderybook.tunerlucky.ui.theme.AccentPurple
import com.spiderybook.tunerlucky.ui.theme.BackgroundBlack
import com.spiderybook.tunerlucky.ui.theme.DangerRed
import com.spiderybook.tunerlucky.ui.theme.GlassOverlay
import com.spiderybook.tunerlucky.ui.theme.HeroGradientEnd
import com.spiderybook.tunerlucky.ui.theme.HeroGradientStart
import com.spiderybook.tunerlucky.ui.theme.SurfaceCard
import com.spiderybook.tunerlucky.ui.theme.SurfacePrimary
import com.spiderybook.tunerlucky.ui.theme.SurfaceSecondary
import com.spiderybook.tunerlucky.ui.theme.TextMuted
import com.spiderybook.tunerlucky.ui.theme.TextPrimary
import com.spiderybook.tunerlucky.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private enum class Section(
    val title: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Default.SpaceDashboard),
    Library("Library", Icons.Default.Folder),
    Dashboard("Dashboard", Icons.Default.Speed),
    Performance("Performance", Icons.Default.Tune),
    Settings("Settings", Icons.Default.Settings),
    Logs("Logs", Icons.Default.Terminal),
    About("About", Icons.Default.Info)
}

@Composable
fun GameSpaceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val libraryManager = remember { LibraryManager(context) }
    val performanceManager = remember { PerformanceManager() }
    val statsMonitor = remember { StatsMonitor(context) }

    val packageNames by libraryManager.libraryApps.collectAsState(initial = emptyList())
    val profiles by libraryManager.gameProfiles.collectAsState(initial = emptyMap())
    val favorites by libraryManager.favoriteApps.collectAsState(initial = emptySet())
    val lastPlayed by libraryManager.lastPlayed.collectAsState(initial = emptyMap())
    val logs by libraryManager.logs.collectAsState(initial = emptyList())
    val fpsCounter by libraryManager.fpsCounter.collectAsState(initial = true)
    val autoDetection by libraryManager.autoDetection.collectAsState(initial = false)
    val autoOverlay by libraryManager.autoOverlay.collectAsState(initial = true)
    val isReady by ShizukuManager.isReady.collectAsState()
    val hasPermission by ShizukuManager.hasPermission.collectAsState()
    val isConnected by ShizukuManager.isServiceConnected.collectAsState()

    val games = remember(packageNames, profiles, favorites, lastPlayed) {
        LibraryGameLoader.loadGames(context, packageNames, profiles, favorites, lastPlayed)
            .sortedWith(compareByDescending<GameInfo> { it.favorite }.thenBy { it.name })
    }

    var selectedSection by remember { mutableStateOf(Section.Home) }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    var showAddGame by remember { mutableStateOf(false) }
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

    val selectedGame = games.firstOrNull { it.packageName == selectedPackage } ?: games.firstOrNull()

    LaunchedEffect(Unit) {
        while (isActive) {
            stats = statsMonitor.read()
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        AuroraBackground()

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Rail(
                selected = selectedSection,
                onSelected = { selectedSection = it }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                TopDashboardBar(
                    stats = stats,
                    shizukuStatus = shizukuStatus(isReady, hasPermission, isConnected),
                    onAdd = { showAddGame = true }
                )

                Spacer(modifier = Modifier.height(18.dp))

                when (selectedSection) {
                    Section.Home -> HomeScreen(
                        games = games,
                        selectedGame = selectedGame,
                        stats = stats,
                        onSelected = { selectedPackage = it.packageName },
                        onLaunch = { game ->
                            if (!android.provider.Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                                scope.launch { libraryManager.addLog("Requiere permiso de superposicion") }
                            } else {
                                scope.launch {
                                    libraryManager.markPlayed(game.packageName)
                                    libraryManager.addLog("${game.name} iniciado")
                                    performanceManager.applyProfile(game.profile)
                                    libraryManager.addLog("Perfil ${game.profile.name} aplicado")
                                }
                                context.packageManager.getLaunchIntentForPackage(game.packageName)?.let {
                                    context.startActivity(it)
                                }
                                context.startForegroundService(Intent(context, OverlayService::class.java))
                            }
                        },
                        onAdd = { showAddGame = true }
                    )

                    Section.Library -> LibraryScreen(
                        games = games,
                        selectedGame = selectedGame,
                        onSelected = { selectedPackage = it.packageName },
                        onAdd = { showAddGame = true },
                        onRemove = { game ->
                            scope.launch {
                                libraryManager.removeApp(game.packageName)
                                libraryManager.addLog("${game.name} eliminado de biblioteca")
                            }
                        },
                        onFavorite = { game ->
                            scope.launch { libraryManager.toggleFavorite(game.packageName) }
                        },
                        onProfile = { game, profile ->
                            scope.launch {
                                libraryManager.setProfile(game.packageName, profile)
                                libraryManager.addLog("${game.name} -> ${profile.name}")
                            }
                        }
                    )

                    Section.Dashboard -> DashboardScreen(
                        stats = stats,
                        shizukuStatus = shizukuStatus(isReady, hasPermission, isConnected)
                    )

                    Section.Performance -> PerformanceScreen(
                        performanceManager = performanceManager,
                        log = { message -> scope.launch { libraryManager.addLog(message) } }
                    )

                    Section.Settings -> SettingsScreen(
                        fpsCounter = fpsCounter,
                        autoDetection = autoDetection,
                        autoOverlay = autoOverlay,
                        onFpsCounter = { scope.launch { libraryManager.setFpsCounter(it) } },
                        onAutoDetection = { scope.launch { libraryManager.setAutoDetection(it) } },
                        onAutoOverlay = { scope.launch { libraryManager.setAutoOverlay(it) } }
                    )

                    Section.Logs -> LogsScreen(logs)
                    Section.About -> AboutScreen()
                }
            }
        }

        AnimatedVisibility(
            visible = showAddGame,
            modifier = Modifier.fillMaxSize()
        ) {
            AddGameScreen(
                libraryPackages = packageNames.toSet(),
                onClose = { showAddGame = false },
                onAdd = { app ->
                    scope.launch {
                        libraryManager.addApp(app.packageName)
                        libraryManager.addLog("${context.packageManager.getApplicationLabel(app)} anadido a biblioteca")
                        selectedPackage = app.packageName
                        showAddGame = false
                    }
                }
            )
        }
    }
}

@Composable
private fun AuroraBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(AccentPurple.copy(alpha = 0.24f), Color.Transparent)
                )
            )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(AccentBlue.copy(alpha = 0.12f), Color.Transparent)
                )
            )
    )
}

@Composable
private fun Rail(
    selected: Section,
    onSelected: (Section) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(86.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassOverlay)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Section.entries.forEach { section ->
            val selectedColor by animateColorAsState(
                targetValue = if (section == selected) AccentBlue else Color.Transparent,
                label = "railColor"
            )
            IconButton(
                onClick = { onSelected(section) },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(selectedColor.copy(alpha = if (section == selected) 0.22f else 0f))
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = section.title,
                    tint = if (section == selected) AccentBlue else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TopDashboardBar(
    stats: StatsData,
    shizukuStatus: String,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "LUCKY TUNER 08",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )
            Text(
                text = "Meizu Lucky 08 Game Space",
                color = TextSecondary
            )
        }

        StatusChip("CPU", stats.cpuFreq)
        Spacer(modifier = Modifier.width(8.dp))
        StatusChip("RAM", stats.ramUsed)
        Spacer(modifier = Modifier.width(8.dp))
        StatusChip("BAT", stats.battery)
        Spacer(modifier = Modifier.width(8.dp))
        StatusChip("Shizuku", shizukuStatus)
        Spacer(modifier = Modifier.width(12.dp))
        Button(onClick = onAdd, shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Anadir")
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(label, color = TextMuted, fontSize = 10.sp)
            Text(value, color = AccentBlue, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
private fun HomeScreen(
    games: List<GameInfo>,
    selectedGame: GameInfo?,
    stats: StatsData,
    onSelected: (GameInfo) -> Unit,
    onLaunch: (GameInfo) -> Unit,
    onAdd: () -> Unit
) {
    Column {
        selectedGame?.let { game ->
            HeroGameCard(game = game, onLaunch = { onLaunch(game) })
        } ?: EmptyLibrary(onAdd)

        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("GPU", stats.gpuFreq, Modifier.weight(1f))
            MetricCard("TEMP", stats.temperature, Modifier.weight(1f))
            MetricCard("FPS", stats.fps, Modifier.weight(1f))
            MetricCard("STORAGE", stats.storage, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))
        LibraryCarousel(games, selectedGame, onSelected, onAdd)
    }
}

@Composable
private fun HeroGameCard(
    game: GameInfo,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(244.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(HeroGradientStart, HeroGradientEnd)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(game.name, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                Text(game.packageName, color = Color.White.copy(alpha = 0.72f), maxLines = 1)
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileChip(game.profile)
                    if (game.favorite) ProfileLabel("FAVORITO")
                    if (game.isEmulator) ProfileLabel("EMULADOR")
                }
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = onLaunch, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("JUGAR")
                }
            }
        }
    }
}

@Composable
private fun LibraryCarousel(
    games: List<GameInfo>,
    selectedGame: GameInfo?,
    onSelected: (GameInfo) -> Unit,
    onAdd: () -> Unit
) {
    Text("Biblioteca Aurora", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        item { AddGameCard(onAdd) }
        items(games, key = { it.packageName }) { game ->
            LibraryGameCard(
                game = game,
                selected = game.packageName == selectedGame?.packageName,
                onClick = { onSelected(game) }
            )
        }
    }
}

@Composable
private fun AddGameCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(158.dp)
            .height(194.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AccentBlue),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(42.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Anadir", color = TextPrimary)
        }
    }
}

@Composable
private fun LibraryGameCard(
    game: GameInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.08f else 1f, label = "gameScale")
    Card(
        modifier = Modifier
            .width(158.dp)
            .height(194.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) AccentBlue else Color.Transparent),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(SurfaceSecondary, AccentPurple.copy(alpha = 0.45f)))),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(packageName = game.packageName, modifier = Modifier.size(64.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(game.name, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(game.profile.name, color = AccentBlue, fontSize = 11.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    games: List<GameInfo>,
    selectedGame: GameInfo?,
    onSelected: (GameInfo) -> Unit,
    onAdd: () -> Unit,
    onRemove: (GameInfo) -> Unit,
    onFavorite: (GameInfo) -> Unit,
    onProfile: (GameInfo, PerformanceProfile) -> Unit
) {
    Column {
        LibraryCarousel(games, selectedGame, onSelected, onAdd)
        Spacer(modifier = Modifier.height(18.dp))
        selectedGame?.let { game ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(game.name, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(game.packageName, color = TextSecondary)
                }
                IconButton(onClick = { onFavorite(game) }) {
                    Icon(
                        if (game.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (game.favorite) DangerRed else TextSecondary
                    )
                }
                IconButton(onClick = { onRemove(game) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PerformanceProfile.entries.forEach { profile ->
                    val selected = game.profile == profile
                    Button(
                        onClick = { onProfile(game, profile) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) AccentBlue else SurfaceCard,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(profile.name, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    stats: StatsData,
    shizukuStatus: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("CPU", stats.cpuFreq, Modifier.weight(1f))
            MetricCard("GPU", stats.gpuFreq, Modifier.weight(1f))
            MetricCard("RAM", stats.ramUsed, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("TEMP", stats.temperature, Modifier.weight(1f))
            MetricCard("BAT", stats.battery, Modifier.weight(1f))
            MetricCard("FPS", stats.fps, Modifier.weight(1f))
        }
        MetricCard("Shizuku Status", shizukuStatus, Modifier.fillMaxWidth())
        MetricCard("Almacenamiento", stats.storage, Modifier.fillMaxWidth())
        LinearProgressIndicator(
            progress = if (shizukuStatus == "Connected") 1f else 0.35f,
            modifier = Modifier.fillMaxWidth(),
            color = AccentBlue,
            trackColor = SurfaceCard
        )
    }
}

@Composable
private fun PerformanceScreen(
    performanceManager: PerformanceManager,
    log: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perfiles del Meizu Lucky 08", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PerformanceProfile.entries.forEach { profile ->
                Button(
                    onClick = {
                        performanceManager.applyProfile(profile)
                        log("Perfil ${profile.name} aplicado manualmente")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(profile.name, fontSize = 11.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("BOOST", Icons.Default.Speed, Modifier.weight(1f)) {
                performanceManager.enableBoost()
                log("Boost activado")
            }
            QuickAction("144HZ", Icons.Default.BatteryChargingFull, Modifier.weight(1f)) {
                performanceManager.set144Hz()
                log("144Hz aplicado")
            }
            QuickAction("RAM", Icons.Default.Tune, Modifier.weight(1f)) {
                performanceManager.clearRam()
                log("RAM cleanup ejecutado")
            }
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = AccentBlue)
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsScreen(
    fpsCounter: Boolean,
    autoDetection: Boolean,
    autoOverlay: Boolean,
    onFpsCounter: (Boolean) -> Unit,
    onAutoDetection: (Boolean) -> Unit,
    onAutoOverlay: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsRow("FPS Counter", fpsCounter, onFpsCounter)
        SettingsRow("Deteccion automatica", autoDetection, onAutoDetection)
        SettingsRow("Overlay automatico", autoOverlay, onAutoOverlay)
        Text("Tema activo: Cyber Blue", color = TextSecondary)
    }
}

@Composable
private fun SettingsRow(
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun LogsScreen(logs: List<String>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(logs.ifEmpty { listOf("Sin eventos todavia") }) { line ->
            Text(
                text = line,
                color = if (logs.isEmpty()) TextMuted else TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun AboutScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Lucky Tuner Game Space", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Dispositivo objetivo: Meizu Lucky 08", color = TextSecondary)
        Text("V1.0: biblioteca manual, perfiles, dashboard real, overlay y Shizuku.", color = TextSecondary)
    }
}

@Composable
private fun EmptyLibrary(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(244.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(SurfacePrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Biblioteca vacia", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Elige manualmente los juegos que apareceran aqui.", color = TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Anadir juego")
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = TextMuted, fontSize = 12.sp)
            Text(value, color = AccentBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun ProfileChip(profile: PerformanceProfile) {
    ProfileLabel(profile.name)
}

@Composable
private fun ProfileLabel(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Text(text, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@Composable
private fun AddGameScreen(
    libraryPackages: Set<String>,
    onClose: () -> Unit,
    onAdd: (ApplicationInfo) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        apps = context.packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { context.packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { context.packageManager.getApplicationLabel(it).toString() }
    }

    val filtered = remember(apps, query, libraryPackages) {
        apps.filter { app ->
            val name = context.packageManager.getApplicationLabel(app).toString()
            val haystack = "$name ${app.packageName}".lowercase()
            app.packageName !in libraryPackages && haystack.contains(query.lowercase())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack.copy(alpha = 0.98f))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Anadir aplicacion", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Solo lo que elijas aparecera en la biblioteca.", color = TextSecondary)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Buscar por nombre o package") }
            )
            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    AddAppRow(app = app, onAdd = { onAdd(app) })
                }
            }
        }
    }
}

@Composable
private fun AddAppRow(
    app: ApplicationInfo,
    onAdd: () -> Unit
) {
    val context = LocalContext.current
    val name = remember(app.packageName) {
        context.packageManager.getApplicationLabel(app).toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .clickable(onClick = onAdd)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(packageName = app.packageName, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(app.packageName, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
        }
        Button(onClick = onAdd, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Anadir")
        }
    }
}

private fun shizukuStatus(
    isReady: Boolean,
    hasPermission: Boolean,
    isConnected: Boolean
): String =
    when {
        isConnected -> "Connected"
        isReady && !hasPermission -> "Permission Missing"
        isReady -> "Disconnected"
        else -> "Not Running"
    }

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap().asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
    } else {
        Icon(Icons.Default.SpaceDashboard, contentDescription = null, modifier = modifier, tint = AccentBlue)
    }
}
