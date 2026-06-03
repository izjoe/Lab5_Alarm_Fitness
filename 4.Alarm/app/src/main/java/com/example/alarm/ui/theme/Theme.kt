package com.example.alarm.ui.theme

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

private val SpaceMissionColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = NeonPink,
    background = SpaceDark,
    surface = SpaceDark,
    surfaceVariant = SpaceCard,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray,
    primaryContainer = SpaceCard,
    onPrimaryContainer = NeonCyan,
    secondaryContainer = NeonPurple,
    onSecondaryContainer = Color.White
)

@Composable
fun AlarmTheme(
    darkTheme: Boolean = true, // Force dark theme for Space Mission
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to maintain theme consistency
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpaceMissionColorScheme,
        typography = Typography,
        content = content
    )
}
