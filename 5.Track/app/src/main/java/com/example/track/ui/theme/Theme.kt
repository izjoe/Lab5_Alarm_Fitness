package com.example.track.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val QuestColorScheme = darkColorScheme(
    primary = QuestGold,
    secondary = QuestTeal,
    tertiary = QuestOrange,
    background = QuestNight,
    surface = QuestSurface,
    onPrimary = QuestNight,
    onBackground = QuestText,
    onSurface = QuestText
)

@Composable
fun TrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QuestColorScheme,
        typography = Typography,
        content = content
    )
}
