package com.example.track.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.track.DashboardUiState
import com.example.track.ai.CoachSuggestion
import com.example.track.model.Badge
import com.example.track.model.FitnessResult
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun FitnessDashboardScreen(
    progress: FitnessResult,
    uiState: DashboardUiState,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onRetrieveProgress: () -> Unit,
    onRequestCoach: () -> Unit
) {
    val unlockedBadges = Badge.unlockedFor(progress.steps)
    val goalProgress = (progress.steps / DAILY_GOAL.toFloat()).coerceIn(0f, 1f)
    var lastMilestone by rememberSaveable { mutableIntStateOf(0) }
    var celebration by remember { mutableStateOf<Badge?>(null) }

    LaunchedEffect(progress.steps) {
        val newest = unlockedBadges.lastOrNull()
        if (newest != null && newest.milestone > lastMilestone) {
            lastMilestone = newest.milestone
            celebration = newest
            delay(2_500)
            celebration = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0E14), Color(0xFF1B1F27), Color(0xFF0A0E14))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SpaceHeader(uiState.tracking)

            AnimatedVisibility(visible = celebration != null, enter = fadeIn(), exit = fadeOut()) {
                celebration?.let { MissionAchievementBanner(it) }
            }

            MissionCircularProgress(progress, goalProgress)

            SpaceMetricsRow(progress)

            MissionControls(
                tracking = uiState.tracking,
                onStartTracking = onStartTracking,
                onStopTracking = onStopTracking,
                onRetrieveProgress = onRetrieveProgress,
                onRequestCoach = onRequestCoach
            )

            if (uiState.tracking) {
                NotificationHint()
            }

            MissionStatusCard(uiState.calculationMessage)

            MissionBadgesSection(unlockedBadges, celebration)

            SpaceCoachCard(uiState.coachSuggestion, uiState.loadingCoach, progress.steps)
        }
    }
}

@Composable
private fun SpaceHeader(tracking: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "SYSTEM STATUS: ONLINE",
                color = Color(0xFFBB86FC),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Star Mission",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("🛸", fontSize = 24.sp)
            }
        }
        Surface(
            color = if (tracking) Color(0xFF03DAC5).copy(alpha = 0.15f) else Color(0xFFBB86FC).copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (tracking) Color(0xFF03DAC5) else Color(0xFFBB86FC))
        ) {
            Text(
                if (tracking) "IN ORBIT" else "LAUNCH READY",
                color = if (tracking) Color(0xFF03DAC5) else Color(0xFFBB86FC),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MissionAchievementBanner(badge: Badge) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBB86FC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("✨", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "MISSION ACHIEVED: ${badge.title}!",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MissionCircularProgress(progress: FitnessResult, goalProgress: Float) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { goalProgress },
                    modifier = Modifier.size(200.dp),
                    color = Color(0xFFBB86FC),
                    strokeWidth = 14.dp,
                    trackColor = Color(0xFF30363D),
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Mission Steps",
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "%,d".format(progress.steps),
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "GOAL: %,d".format(DAILY_GOAL),
                        color = Color(0xFFBB86FC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                spaceMotivation(progress.steps),
                color = Color(0xFFC9D1D9),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SpaceMetricsRow(progress: FitnessResult) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SpaceMetricCard(
            Modifier.weight(1f),
            "Energy Burned",
            String.format(Locale.US, "%.1f", progress.calories),
            "KCAL",
            Color(0xFFCF6679)
        )
        SpaceMetricCard(
            Modifier.weight(1f),
            "XP Earned",
            progress.points.toString(),
            "XP",
            Color(0xFF03DAC5)
        )
    }
}

@Composable
private fun SpaceMetricCard(modifier: Modifier, label: String, value: String, suffix: String, accent: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(" $suffix", color = Color(0xFF8B949E), modifier = Modifier.padding(bottom = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MissionControls(
    tracking: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onRetrieveProgress: () -> Unit,
    onRequestCoach: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onStartTracking,
                enabled = !tracking,
                modifier = Modifier.weight(1.2f).height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBB86FC),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("LAUNCH MISSION", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            OutlinedButton(
                onClick = onStopTracking,
                enabled = tracking,
                modifier = Modifier.weight(0.8f).height(60.dp),
                border = BorderStroke(2.dp, if (tracking) Color(0xFFCF6679) else Color(0xFF30363D)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCF6679))
            ) {
                Text("END MISSION", fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onRetrieveProgress,
                modifier = Modifier.weight(1f).height(54.dp),
                border = BorderStroke(1.dp, Color(0xFF8B949E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("SCAN PROGRESS", color = Color.White, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onRequestCoach,
                modifier = Modifier.weight(1f).height(54.dp),
                border = BorderStroke(1.dp, Color(0xFF03DAC5)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF03DAC5))
            ) {
                Text("ASK SPACE COACH", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun NotificationHint() {
    Surface(
        color = Color(0xFF03DAC5).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF03DAC5)))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Live mission notification is active while tracking.",
                color = Color(0xFF03DAC5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MissionStatusCard(message: String) {
    Surface(
        color = Color(0xFF0D1117),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = Color(0xFF8B949E),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun MissionBadgesSection(unlocked: List<Badge>, celebration: Badge?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("MISSION BADGES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
        Badge.entries.chunked(2).forEach { rowBadges ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowBadges.forEach { badge ->
                    MissionBadgeCard(badge, badge in unlocked, badge == celebration, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MissionBadgeCard(badge: Badge, unlocked: Boolean, celebrate: Boolean, modifier: Modifier) {
    val scale by animateFloatAsState(if (celebrate) 1.05f else 1f, spring(), label = "badgeScale")
    Card(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) Color(0xFFBB86FC).copy(alpha = 0.1f) else Color(0xFF161B22)
        ),
        border = BorderStroke(1.dp, if (unlocked) Color(0xFFBB86FC) else Color(0xFF30363D)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (unlocked) "RANK ATTAINED" else "CLASSIFIED",
                color = if (unlocked) Color(0xFFBB86FC) else Color(0xFF484F58),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(badge.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text("%,d STEPS".format(badge.milestone), color = Color(0xFF8B949E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SpaceCoachCard(suggestion: CoachSuggestion?, loading: Boolean, steps: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF03DAC5).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛰️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("SPACE COACH ADVICE", color = Color(0xFF03DAC5), fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            }
            Text(
                when {
                    loading -> "Establishing neural link with Gemini Station..."
                    suggestion != null -> suggestion.text
                    else -> "Engines at standby. ${remainingSpaceSteps(steps)} steps until next orbital jump."
                },
                color = Color.White,
                lineHeight = 24.sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun remainingSpaceSteps(steps: Int) = "%,d".format((DAILY_GOAL - steps).coerceAtLeast(0))

private fun spaceMotivation(steps: Int): String = when {
    steps >= 15_000 -> "Deep Space Legend status achieved! Galaxy Explorer."
    steps >= 10_000 -> "Galaxy Runner! Mission objective cleared."
    steps >= 5_000 -> "Orbit Walker unlocked. Approaching outer rim."
    steps >= 1_000 -> "Rookie Explorer! Ignition confirmed."
    else -> "Ignite your engines and reach your first orbit."
}

private const val DAILY_GOAL = 10_000
