package com.spiderybook.tunerlucky.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LuckyTunerDarkScheme = darkColorScheme(

    primary = AccentBlue,
    secondary = AccentPurple,

    background = BackgroundBlack,
    surface = SurfacePrimary,
    surfaceVariant = SurfaceSecondary,

    onPrimary = TextPrimary,
    onSecondary = TextPrimary,

    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun TunerLuckyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LuckyTunerDarkScheme,
        content = content
    )
}