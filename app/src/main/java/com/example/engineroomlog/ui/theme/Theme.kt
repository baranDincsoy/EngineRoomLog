package com.example.engineroomlog.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyPrimaryContainer,
    onPrimaryContainer = Color(0xFF001D33),
    secondary = SteelSecondary,
    onSecondary = Color.White,
    secondaryContainer = SteelSecondaryContainer,
    tertiary = InstrumentTeal,
    onTertiary = Color.White,
    tertiaryContainer = InstrumentTealContainer,
    error = AlarmRed,
    onError = Color.White,
    errorContainer = AlarmRedContainer,
    background = SurfaceLight,
    onBackground = Color(0xFF191C1E),
    surface = SurfaceLight,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF41484D),
    outline = OutlineLight
)

private val DarkColors = darkColorScheme(
    primary = NavyPrimaryDark,
    onPrimary = Color(0xFF003353),
    primaryContainer = NavyPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = SteelSecondaryDark,
    onSecondary = Color(0xFF1E3346),
    secondaryContainer = SteelSecondaryContainerDark,
    tertiary = InstrumentTealDark,
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = InstrumentTealContainerDark,
    error = AlarmRedDark,
    onError = Color(0xFF601410),
    errorContainer = AlarmRedContainerDark,
    background = SurfaceDark,
    onBackground = Color(0xFFE1E3E5),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFBFC8CE),
    outline = OutlineDark
)
@Composable
fun EngineRoomLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}