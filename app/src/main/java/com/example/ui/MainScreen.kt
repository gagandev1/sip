package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.components.CelebrationOverlay
import com.example.ui.components.glass.FloatingGlassNavBar
import com.example.ui.components.glass.LiquidBackground
import com.example.ui.screens.*
import com.example.viewmodel.SipViewModel

enum class SipTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.WaterDrop),
    STATS("Stats", Icons.Rounded.BarChart),
    HEALTH("Health", Icons.Rounded.Favorite),
    COACH("Coach", Icons.Rounded.AutoAwesome),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

@Composable
fun MainScreen(
    viewModel: SipViewModel,
    tiltFactor: Float,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(SipTab.HOME) }
    val showCelebration by viewModel.showCelebration.collectAsState()
    val waterLogs by viewModel.waterLogs.collectAsState()
    val profile by viewModel.profile.collectAsState()

    val todayIntake = remember(waterLogs) { viewModel.getTodayIntakeMl() }
    val progress = remember(todayIntake, profile.dailyGoalMl) {
        todayIntake.toFloat() / profile.dailyGoalMl.coerceAtLeast(1).toFloat()
    }
    val (streak, _) = remember(waterLogs) { viewModel.calculateStreak() }

    BackHandler(enabled = currentTab != SipTab.HOME) {
        currentTab = SipTab.HOME
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Living, colorful Liquid Glass Aurora Background
        LiquidBackground(
            progress = progress,
            themeMode = profile.themeMode,
            reduceMotion = profile.reduceMotion,
            modifier = Modifier.fillMaxSize()
        )

        // Main Screen Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(durationMillis = if (profile.reduceMotion) 150 else 240),
                label = "TabCrossfade"
            ) { tab ->
                when (tab) {
                    SipTab.HOME -> HomeScreen(viewModel = viewModel, tiltFactor = tiltFactor)
                    SipTab.STATS -> StatsScreen(viewModel = viewModel)
                    SipTab.HEALTH -> HealthScreen(viewModel = viewModel)
                    SipTab.COACH -> CoachScreen(viewModel = viewModel)
                    SipTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Floating Liquid Glass Navigation Bar (inset from edges and above safe navigation insets)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 6.dp)
        ) {
            FloatingGlassNavBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it },
                glassOpacity = profile.glassOpacity,
                reduceTransparency = profile.reduceTransparency
            )
        }

        // Celebration Overlay on Goal Smashed
        if (showCelebration) {
            CelebrationOverlay(
                streakDays = streak.coerceAtLeast(1),
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}
