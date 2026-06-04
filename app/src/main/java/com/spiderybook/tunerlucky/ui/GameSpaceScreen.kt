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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
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
    About("About", Icons.Default.Info)
}

private enum class ShowAllFilter(val label: String) {
    ShowAll("Show All"),
    Android("Android"),
    Emulators("Emulators")
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
    var showAllFilter by remember { mutableStateOf(ShowAllFilter.ShowAll) }

    val filteredGames = remember(games, showAllFilter) {
        when (showAllFilter) {
            ShowAllFilter.ShowAll -> games
            ShowAllFilter.Android -> games.filter { !it.isEmulator }
            ShowAllFilter.Emulators -> games.filter { it.isEmulator }
        }
    }
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

        // Main content area with padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 66.dp) // Reserve space for bottom bar
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            AuroraHeader(
                filter = showAllFilter,
                onFilterChange = { showAllFilter = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(modifier = Modifier.weight(1f)) {

                when (selectedSection) {
                    Section.Home -> HomeScreen(
                        games = filteredGames,
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
                        games = filteredGames,
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

                    Section.About -> AboutScreen()
                }
            } // Close Box
        }

        // Bottom Navigation pinned to very bottom edge
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNavigationMenu(
                selected = selectedSection,
                onSelected = { selectedSection = it }
            )
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
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1000f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset2"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0D47A1), Color.Transparent),
                center = Offset(offset1 % w, h * 0.3f),
                radius = w * 0.8f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00BCD4).copy(alpha=0.4f), Color.Transparent),
                center = Offset(w - (offset2 % w), h * 0.7f),
                radius = w * 0.9f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4A148C).copy(alpha=0.3f), Color.Transparent),
                center = Offset(w / 2f, offset1 % h),
                radius = w * 0.7f
            )
        )
    }
}

@Composable
private fun BottomNavigationMenu(
    selected: Section,
    onSelected: (Section) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(GlassOverlay)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Section.entries.forEach { section ->
            val isSelected = section == selected
            val selectedColor by animateColorAsState(
                targetValue = if (isSelected) AccentBlue else Color.Transparent,
                label = "navColor"
            )
            IconButton(
                onClick = { onSelected(section) },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(selectedColor.copy(alpha = if (isSelected) 0.22f else 0f))
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = section.title,
                    tint = if (isSelected) AccentBlue else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AuroraHeader(
    filter: ShowAllFilter,
    onFilterChange: (ShowAllFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Profile side
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            Column {
                Text("Fran", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("G 200 / 2", color = Color.White, fontSize = 14.sp)
                }
            }
        }
        
        // Show All / Android / Emulators toggle
        Text(
            text = filter.label,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val next = when (filter) {
                        ShowAllFilter.ShowAll -> ShowAllFilter.Android
                        ShowAllFilter.Android -> ShowAllFilter.Emulators
                        ShowAllFilter.Emulators -> ShowAllFilter.ShowAll
                    }
                    onFilterChange(next)
                }
                .padding(8.dp)
        )
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    games: List<GameInfo>,
    selectedGame: GameInfo?,
    stats: StatsData,
    onSelected: (GameInfo) -> Unit,
    onLaunch: (GameInfo) -> Unit,
    onAdd: () -> Unit
) {
    if (games.isEmpty()) {
        EmptyLibrary(onAdd)
        return
    }

    val pagerState = rememberPagerState(pageCount = { games.size + 1 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < games.size) {
            onSelected(games[pagerState.currentPage])
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(320.dp),
            contentPadding = PaddingValues(horizontal = 60.dp),
            pageSpacing = (-8).dp // Overlap slightly like Aurora
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val scale = 1f - 0.15f * kotlin.math.abs(pageOffset).coerceAtMost(1f)
            val alpha = 1f - 0.3f * kotlin.math.abs(pageOffset).coerceAtMost(1f)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .width(160.dp)
                    .height(280.dp)
                    .clickable {
                        if (page == pagerState.currentPage) {
                            if (page < games.size) onLaunch(games[page]) else onAdd()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(page) }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (page < games.size) {
                    AuroraGameCover(games[page])
                } else {
                    AddGameCover()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transparent background text
        if (pagerState.currentPage < games.size) {
            val game = games[pagerState.currentPage]
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Medium)
                Text("${pagerState.currentPage + 1} de ${games.size}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Anadir Nuevo", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AuroraGameCover(game: GameInfo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Main Cover - Xbox360 style tall box
        Card(
            modifier = Modifier.fillMaxWidth().height(240.dp),
            shape = RoundedCornerShape(4.dp), // Sharp corners like real Xbox cases
            border = BorderStroke(2.dp, Color(0xFF4CAF50)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Android Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "  ANDROID",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
                // Cover body with game icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2B2B2B), Color(0xFF1A1A2E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(packageName = game.packageName, modifier = Modifier.size(72.dp))
                }
            }
        }
        
        // Reflection Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .graphicsLayer {
                    scaleY = -1f
                    alpha = 0.2f
                }
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF2B2B2B), Color(0xFF1A1A2E)))),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(packageName = game.packageName, modifier = Modifier.size(72.dp))
                }
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundBlack))))
        }
    }
}

@Composable
private fun AddGameCover() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, Color.Gray),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
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
        Text("Biblioteca", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(games, key = { it.packageName }) { game ->
                val selected = game.packageName == selectedGame?.packageName
                val scale by animateFloatAsState(targetValue = if (selected) 1.08f else 1f, label = "gameScale")
                Card(
                    modifier = Modifier
                        .width(158.dp)
                        .height(194.dp)
                        .scale(scale)
                        .clickable(onClick = { onSelected(game) }),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) AccentBlue else Color.Transparent),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(108.dp)
                                .clip(RoundedCornerShape(8.dp))
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
        }
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
fun LibraryCarousel(x0: List<GameInfo>, x1: GameInfo?, x2: (GameInfo) -> Unit, x3: () -> Unit) {
    TODO("Not yet implemented")
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
