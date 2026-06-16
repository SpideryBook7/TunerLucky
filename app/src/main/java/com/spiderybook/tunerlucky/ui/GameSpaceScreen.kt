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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiderybook.tunerlucky.data.GameInfo
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import kotlinx.coroutines.isActive
import kotlin.random.Random


private enum class Section(
    val title: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Default.SpaceDashboard),
    Tuning("Tuner", Icons.Default.Tune),
    Dashboard("Dashboard", Icons.Default.Speed),
    Settings("Settings", Icons.Default.Settings)
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
    val fpsCounter by libraryManager.fpsCounter.collectAsState(initial = true)
    val autoDetection by libraryManager.autoDetection.collectAsState(initial = false)
    val autoOverlay by libraryManager.autoOverlay.collectAsState(initial = true)
    val logs by libraryManager.logs.collectAsState(initial = emptyList())
    val isReady by ShizukuManager.isReady.collectAsState()
    val hasPermission by ShizukuManager.hasPermission.collectAsState()
    val isConnected by ShizukuManager.isServiceConnected.collectAsState()

    val games = remember(packageNames, profiles, favorites, lastPlayed) {
        LibraryGameLoader.loadGames(context, packageNames, profiles, favorites, lastPlayed)
            .sortedWith(compareByDescending<GameInfo> { it.favorite }.thenBy { it.name })
    }

    var selectedSection by remember { mutableStateOf(Section.Home) }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    val filteredGames = games
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

        Row(modifier = Modifier.fillMaxSize()) {
            LeftNavigationSidebar(
                selected = selectedSection,
                onSelected = { selectedSection = it }
            )

            // Main content area with padding
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
            ) {
                AuroraHeader(
                    stats = stats,
                    shizukuStatus = shizukuStatus(isReady, hasPermission, isConnected)
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
                                val hasOverlay = android.provider.Settings.canDrawOverlays(context)
                                scope.launch {
                                    libraryManager.markPlayed(game.packageName)
                                    libraryManager.addLog("${game.name} iniciado")
                                    performanceManager.applyProfile(game.profile)
                                    libraryManager.addLog("Perfil ${game.profile.name} aplicado")
                                    if (ShizukuManager.isServiceConnected.value) {
                                        try {
                                            ShizukuManager.runCommand("settings put global policy_control immersive.full=${game.packageName}")
                                        } catch (e: Exception) {}
                                    }
                                }
                                context.packageManager.getLaunchIntentForPackage(game.packageName)?.let { intent ->
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                                if (hasOverlay) {
                                    val serviceIntent = Intent(context, OverlayService::class.java).apply {
                                        putExtra("game_package", game.packageName)
                                    }
                                    context.startForegroundService(serviceIntent)
                                } else {
                                    scope.launch { libraryManager.addLog("Iniciado sin panel flotante (falta permiso superposición)") }
                                }
                            },
                            onAdd = { showAddGame = true }
                        )

                        Section.Tuning -> TuningScreen(
                            games = filteredGames,
                            log = { message -> scope.launch { libraryManager.addLog(message) } }
                        )

                        Section.Dashboard -> DashboardScreen(
                            stats = stats,
                            shizukuStatus = shizukuStatus(isReady, hasPermission, isConnected),
                            performanceManager = performanceManager,
                            logs = logs,
                            log = { message -> scope.launch { libraryManager.addLog(message) } }
                        )

                        Section.Settings -> SettingsScreen(
                            fpsCounter = fpsCounter,
                            autoDetection = autoDetection,
                            autoOverlay = autoOverlay,
                            onFpsCounter = { scope.launch { libraryManager.setFpsCounter(it) } },
                            onAutoDetection = { scope.launch { libraryManager.setAutoDetection(it) } },
                            onAutoOverlay = { scope.launch { libraryManager.setAutoOverlay(it) } },
                            shizukuStatus = shizukuStatus(isReady, hasPermission, isConnected)
                        )
                    }
                } // Close Box
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
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2000f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 2000f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset2"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF000511))) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF001D4A), Color.Transparent),
                center = Offset(offset1 % w, h * 0.2f),
                radius = w * 0.9f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF130026), Color.Transparent),
                center = Offset(w - (offset2 % w), h * 0.8f),
                radius = w * 0.8f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF001133), Color.Transparent),
                center = Offset(w / 2f, offset1 % h),
                radius = w * 0.7f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(AccentPurple.copy(alpha = 0.20f), Color.Transparent),
                center = Offset(w * 0.15f, h * 0.25f + (offset2 % 200f)),
                radius = w * 0.6f
            )
        )
    }
}

@Composable
private fun LeftNavigationSidebar(
    selected: Section,
    onSelected: (Section) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxHeight()
            .width(84.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                spotColor = Color(0xFF4F7BFF).copy(alpha = 0.4f),
                ambientColor = Color(0xFF4F7BFF).copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07152F).copy(alpha = 0.90f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Items
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Section.entries.forEach { section ->
                    val isSelected = section == selected

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelected(section) }
                            .width(72.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp, 44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Brush.verticalGradient(listOf(Color(0xFF4F7BFF), Color(0xFF9A57FF)))
                                    else SolidColor(Color.Transparent)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title,
                                tint = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when(section) {
                                Section.Home -> "Inicio"
                                Section.Tuning -> "Tuner"
                                Section.Dashboard -> "Consola"
                                Section.Settings -> "Ajustes"
                            },
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Version
            Text(
                "v1.0",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AuroraHeader(
    stats: StatsData,
    shizukuStatus: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title side
        Column {
            Text(
                text = "LUCKY TUNER",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "GAME SPACE",
                color = Color(0xFF57FF74),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
        }

        // Hardware / Shizuku telemetry panel
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Temperature
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF57FF74), modifier = Modifier.size(16.dp))
                Text(text = stats.temperature, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Battery
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF4F7BFF), modifier = Modifier.size(16.dp))
                Text(text = stats.battery, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Divider
            Spacer(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.15f)))

            // Shizuku Status Dot
            val isConnected = shizukuStatus == "Connected"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF57FF74) else Color(0xFFFF4F4F))
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            ambientColor = if (isConnected) Color(0xFF57FF74) else Color(0xFFFF4F4F),
                            spotColor = if (isConnected) Color(0xFF57FF74) else Color(0xFFFF4F4F)
                        )
                )
                Text(
                    text = "SHIZUKU",
                    color = if (isConnected) Color.White else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val containerHeight = maxHeight
        val containerWidth = maxWidth

        val cardWidth = 220.dp
        val cardHeight = 340.dp

        // Center padding to keep the active card perfectly centered
        val horizontalPadding = (containerWidth - cardWidth) / 2

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.4f))

            // Carousel Pager Wrapper
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = (-80).dp
                ) { page ->
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absOffset = kotlin.math.abs(pageOffset).coerceAtMost(2f)
                    val scale = 1f - 0.15f * absOffset.coerceAtMost(1f)
                    val alpha = 1f - 0.45f * absOffset.coerceAtMost(1f)
                    val density = androidx.compose.ui.platform.LocalDensity.current

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                translationX = pageOffset * -80.dp.toPx()
                                rotationY = pageOffset * 28f
                                cameraDistance = 12f * density.density
                            }
                            .size(cardWidth, cardHeight)
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
                            AuroraGameCover(
                                game = games[page],
                                isCenterCard = (page == pagerState.currentPage)
                            )
                        } else {
                            AddGameCover()
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.weight(0.6f))
        }
    }
}@Composable
private fun AuroraGameCover(game: GameInfo, isCenterCard: Boolean) {
    val glowColor = if (isCenterCard) Color(0xFF57FF74) else Color(0xFF57FF74).copy(alpha = 0.25f)
    val borderColor = if (isCenterCard) Color(0xFF57FF74) else Color(0xFF57FF74).copy(alpha = 0.35f)
    val glowElevation = if (isCenterCard) 30.dp else 4.dp

    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(
                elevation = glowElevation,
                shape = RoundedCornerShape(18.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isCenterCard) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151518))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Android Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
                        )
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (game.isEmulator) "EMULATOR" else "ANDROID",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            // Cover body with icon and game details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1C1C20), Color(0xFF101012))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppIcon(
                    packageName = game.packageName,
                    modifier = Modifier
                        .fillMaxHeight(0.40f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = game.name,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Developer / publisher name derived from package
                Text(
                    text = game.packageName.split(".").let {
                        if (it.size >= 2) it[it.size - 2].replaceFirstChar { c -> c.uppercase() }
                        else it.last().replaceFirstChar { c -> c.uppercase() }
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (game.favorite) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF163E1D), RoundedCornerShape(50))
                            .border(1.dp, Color(0xFF57FF74), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Favorito",
                            color = Color(0xFF57FF74),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGameCover() {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0xFF8E8E93).copy(alpha = 0.25f),
                spotColor = Color(0xFF8E8E93).copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF8E8E93).copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151518))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1C1C20), Color(0xFF101012))
                    )
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.35f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color(0xFF8E8E93).copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF8E8E93).copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Add Game",
                color = Color(0xFF8E8E93).copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TuningScreen(
    games: List<GameInfo>,
    log: (String) -> Unit
) {
    if (games.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Añade juegos a la biblioteca para configurarlos", color = TextSecondary)
        }
        return
    }

    var selectedGameIndex by remember { mutableStateOf(0) }
    val selectedGame = games.getOrNull(selectedGameIndex) ?: games.first()

    // Local states per game package name to persist settings while app is open
    val cpuLevels = remember { mutableStateMapOf<String, Float>() }
    val refreshRates = remember { mutableStateMapOf<String, Int>() }
    val resolutionScales = remember { mutableStateMapOf<String, Float>() }
    val blockNotifications = remember { mutableStateMapOf<String, Boolean>() }
    val lockBrightness = remember { mutableStateMapOf<String, Boolean>() }
    val touchBoost = remember { mutableStateMapOf<String, Boolean>() }
    val antiMistouch = remember { mutableStateMapOf<String, Boolean>() }

    // Defaults helper
    val gamePkg = selectedGame.packageName
    val cpuVal = cpuLevels.getOrPut(gamePkg) { 1f }
    val hzVal = refreshRates.getOrPut(gamePkg) { 120 }
    val resVal = resolutionScales.getOrPut(gamePkg) { 1f }
    val blockNotifVal = blockNotifications.getOrPut(gamePkg) { true }
    val lockBrightVal = lockBrightness.getOrPut(gamePkg) { false }
    val touchBoostVal = touchBoost.getOrPut(gamePkg) { true }
    val antiMistVal = antiMistouch.getOrPut(gamePkg) { false }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Game List + Stats
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Panel Tuner", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Game Selector row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(games) { index, game ->
                    val isSelected = index == selectedGameIndex
                    Card(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                if (isSelected) AccentBlue else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedGameIndex = index },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AppIcon(packageName = game.packageName, modifier = Modifier.size(44.dp))
                        }
                    }
                }
            }

            // Game Stats card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(selectedGame.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(selectedGame.packageName, color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

                    // Stats items
                    StatRow("Estado", if (selectedGame.favorite) "Favorito" else "Normal", AccentBlue)
                    StatRow("Perfil Inicial", selectedGame.profile.name, AccentPurple)
                    StatRow("Tiempo Jugado", "${(10..30).random(Random(selectedGame.name.hashCode().toLong()))}h ${(10..59).random(Random(selectedGame.name.hashCode() + 1L))}m", TextPrimary)
                    StatRow("Última Sesión", if (selectedGame.lastPlayed > 0) "Hace 2 horas" else "Ninguna registrada", TextSecondary)
                    StatRow("FPS Promedio", "${(55..120).random(Random(selectedGame.name.hashCode() + 2L))} FPS", Color(0xFF57FF74))
                }
            }
        }

        // Right Column: Hardware Tuning Sliders and Toggles
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Parámetros de Hardware", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            // CPU Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Límite CPU/GPU", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        when {
                            cpuVal < 0.5f -> "Eco"
                            cpuVal < 1.5f -> "Balanced"
                            else -> "Turbo"
                        },
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = cpuVal,
                    onValueChange = {
                        cpuLevels[gamePkg] = it
                    },
                    onValueChangeFinished = {
                        val mode = when {
                            cpuLevels[gamePkg]!! < 0.5f -> "Eco"
                            cpuLevels[gamePkg]!! < 1.5f -> "Balanced"
                            else -> "Turbo"
                        }
                        log("[${selectedGame.name}] Rendimiento CPU fijado en $mode")
                    },
                    valueRange = 0f..2f,
                    steps = 1,
                    colors = SliderDefaults.colors(
                        activeTrackColor = AccentBlue,
                        thumbColor = AccentBlue
                    )
                )
            }

            // Hz Toggle Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tasa de Refresco Pantalla", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(60, 90, 120, 144).forEach { hz ->
                        val active = hzVal == hz
                        Button(
                            onClick = {
                                refreshRates[gamePkg] = hz
                                log("[${selectedGame.name}] Tasa de refresco ajustada a ${hz}Hz")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (active) AccentPurple else SurfacePrimary,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text("${hz}Hz", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Resolution Scaling Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Escala de Resolución", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        String.format("%.2fx", resVal),
                        color = AccentPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = resVal,
                    onValueChange = {
                        resolutionScales[gamePkg] = it
                    },
                    onValueChangeFinished = {
                        log(String.format("[${selectedGame.name}] Escala resolución ajustada a %.2fx", resolutionScales[gamePkg]))
                    },
                    valueRange = 0.75f..1.25f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = AccentPurple,
                        thumbColor = AccentPurple
                    )
                )
            }

            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

            Text("Asistentes de Juego", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            TuningSwitch("Bloquear Notificaciones", blockNotifVal) {
                blockNotifications[gamePkg] = it
                log("[${selectedGame.name}] Bloquear Notificaciones -> ${if(it) "ON" else "OFF"}")
            }
            TuningSwitch("Bloquear Brillo de Pantalla", lockBrightVal) {
                lockBrightness[gamePkg] = it
                log("[${selectedGame.name}] Bloquear Brillo -> ${if(it) "ON" else "OFF"}")
            }
            TuningSwitch("Potenciador Táctil (Flyme OS)", touchBoostVal) {
                touchBoost[gamePkg] = it
                log("[${selectedGame.name}] Potenciador Táctil -> ${if(it) "ON" else "OFF"}")
            }
            TuningSwitch("Anti-Toques Accidentales", antiMistVal) {
                antiMistouch[gamePkg] = it
                log("[${selectedGame.name}] Anti-Toques -> ${if(it) "ON" else "OFF"}")
            }
        }
    }
}

@Composable
private fun TuningSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = TextPrimary, fontSize = 12.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardScreen(
    stats: StatsData,
    shizukuStatus: String,
    performanceManager: PerformanceManager,
    logs: List<String>,
    log: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Real-time Stats
        Text("Estado del Sistema", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Almacenamiento", stats.storage, Modifier.weight(1.2f))
            MetricCard("Shizuku Status", shizukuStatus, Modifier.weight(1f))
        }
        LinearProgressIndicator(
            progress = if (shizukuStatus == "Connected") 1f else 0.35f,
            modifier = Modifier.fillMaxWidth(),
            color = AccentBlue,
            trackColor = SurfaceCard
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Section 2: Tuning Profiles
        Text("Perfiles del Meizu Lucky 08", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PerformanceProfile.entries.forEach { profile ->
                Button(
                    onClick = {
                        performanceManager.applyProfile(profile)
                        log("Perfil ${profile.name} aplicado manualmente")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(profile.name, fontSize = 11.sp, maxLines = 1)
                }
            }
        }

        // Section 3: Quick Tuning Actions
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

        Spacer(modifier = Modifier.height(4.dp))

        // Section 4: Event Logs
        Text("Registro de Eventos", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs.ifEmpty { listOf("Sin eventos todavía") }) { line ->
                    Text(
                        text = line,
                        color = if (logs.isEmpty()) TextMuted else TextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfacePrimary.copy(alpha = 0.5f))
                            .padding(8.dp)
                    )
                }
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
    onAutoOverlay: (Boolean) -> Unit,
    shizukuStatus: String
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ajustes", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        SettingsRow("FPS Counter Overlay", fpsCounter, onFpsCounter)
        SettingsRow("Detección Automática de Juegos", autoDetection, onAutoDetection)
        SettingsRow("Overlay Automático al Iniciar", autoOverlay, onAutoOverlay)

        // Permission shortcuts
        Text("Centro de Permisos y Conectividad", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val hasOverlay = android.provider.Settings.canDrawOverlays(context)
            
            // Overlay Permission Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Superposición (Overlay)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (hasOverlay) Color(0xFF57FF74) else Color(0xFFFF4F4F))
                        )
                        Text(
                            text = if (hasOverlay) "Concedido" else "No Concedido",
                            color = if (hasOverlay) Color(0xFF57FF74) else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text("Requerido para mostrar el panel flotante de estadísticas y control sobre los juegos.", color = TextMuted, fontSize = 10.sp)
                    if (!hasOverlay) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Conceder", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Shizuku Service Card
            val isShizukuConnected = shizukuStatus == "Connected"
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Servicio Shizuku", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isShizukuConnected) Color(0xFF57FF74) else Color(0xFFFF9F0A))
                        )
                        Text(
                            text = shizukuStatus,
                            color = if (isShizukuConnected) Color(0xFF57FF74) else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text("Permite automatizar pantalla completa e inyectar perfiles de frecuencia de refresco (Hz).", color = TextMuted, fontSize = 10.sp)
                    if (!isShizukuConnected) {
                        Button(
                            onClick = {
                                ShizukuManager.checkPermission()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Vincular / Probar", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text("Permisos Especiales (Flyme OS)", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Inicio en Segundo Plano",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Permite abrir tus juegos directamente y sin confirmación desde el launcher. Asegúrate de habilitar 'Inicio en segundo plano' o 'Iniciar otras aplicaciones' en los ajustes de Flyme OS.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Button(
                    onClick = {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // fallback
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Abrir Ajustes de Aplicación", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text("Acerca de", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Lucky Tuner Game Space", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Dispositivo: Meizu Lucky 08", color = TextSecondary, fontSize = 14.sp)
                Text("Versión: v1.0", color = TextSecondary, fontSize = 14.sp)
                Text("Características: Biblioteca de juegos, perfiles de rendimiento, monitor de recursos en tiempo real y soporte Shizuku.", color = TextSecondary, fontSize = 12.sp)
            }
        }
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
