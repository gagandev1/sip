package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.LiquidGlass
import com.example.ui.theme.SipAquaPrimary
import com.example.ui.theme.SipMintAccent
import com.example.viewmodel.SipViewModel

@Composable
fun OnboardingScreen(
    viewModel: SipViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    val profile by viewModel.profile.collectAsState()

    // State for Step 2
    var weightKg by remember { mutableFloatStateOf(profile.weightKg) }
    var heightCm by remember { mutableFloatStateOf(profile.heightCm) }
    var age by remember { mutableIntStateOf(profile.age) }
    var activityLevel by remember { mutableStateOf(profile.activityLevel) }
    var climate by remember { mutableStateOf(profile.climate) }
    var wakeUpTime by remember { mutableStateOf(profile.wakeUpTime) }
    var bedtime by remember { mutableStateOf(profile.bedtime) }

    // Calculated live goal
    val suggestedGoal = remember(weightKg, activityLevel, climate) {
        UserProfile.calculateSuggestedGoalMl(weightKg, activityLevel, climate)
    }
    var customizedGoal by remember(suggestedGoal) { mutableIntStateOf(suggestedGoal) }

    // State for Step 3
    var reminderMode by remember { mutableStateOf(profile.reminderMode) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen_container")
    ) {
        // Living liquid background for onboarding
        com.example.ui.components.glass.LiquidBackground(
            progress = 0.5f,
            themeMode = profile.themeMode,
            reduceMotion = profile.reduceMotion,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header: Step indicators and Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == step) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (index == step) SipAquaPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                if (step < 3) {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding(
                                profile.copy(
                                    weightKg = weightKg,
                                    heightCm = heightCm,
                                    age = age,
                                    activityLevel = activityLevel,
                                    climate = climate,
                                    dailyGoalMl = customizedGoal,
                                    reminderMode = reminderMode
                                )
                            )
                            onFinish()
                        },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "OnboardingSteps"
            ) { targetStep ->
                when (targetStep) {
                    1 -> OnboardingStepWelcome()
                    2 -> OnboardingStepAboutYou(
                        weightKg = weightKg,
                        onWeightChange = { weightKg = it },
                        activityLevel = activityLevel,
                        onActivityChange = { activityLevel = it },
                        climate = climate,
                        onClimateChange = { climate = it },
                        calculatedGoal = customizedGoal,
                        onGoalChange = { customizedGoal = it }
                    )
                    3 -> OnboardingStepReminders(
                        reminderMode = reminderMode,
                        onReminderModeChange = { reminderMode = it },
                        permissionGranted = notificationPermissionGranted,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                notificationPermissionGranted = true
                            }
                        }
                    )
                }
            }

            // Bottom Action Button
            Button(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        viewModel.completeOnboarding(
                            profile.copy(
                                weightKg = weightKg,
                                heightCm = heightCm,
                                age = age,
                                activityLevel = activityLevel,
                                climate = climate,
                                dailyGoalMl = customizedGoal,
                                reminderMode = reminderMode
                            )
                        )
                        onFinish()
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SipAquaPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_next_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (step == 3) "Start Hydrating" else "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (step == 3) Icons.Rounded.Check else Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepWelcome() {
    // Continuous filling loop for the tumbler glass
    val infiniteTransition = rememberInfiniteTransition(label = "FillingLoop")
    val animatedLoopFill by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LoopFill"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LiquidGlass(
            currentMl = (animatedLoopFill * 2500).toInt(),
            goalMl = 2500,
            tiltFactor = 0f,
            isPouring = false,
            onLongPress = {},
            modifier = Modifier
                .size(270.dp)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Sip",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Light,
                color = SipAquaPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Stay hydrated. Feel better.",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "A mindful, minimal companion designed to build a joyful, effortless water habit without nagging or clutter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun OnboardingStepAboutYou(
    weightKg: Float,
    onWeightChange: (Float) -> Unit,
    activityLevel: ActivityLevel,
    onActivityChange: (ActivityLevel) -> Unit,
    climate: Climate,
    onClimateChange: (Climate) -> Unit,
    calculatedGoal: Int,
    onGoalChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Personalize Your Plan",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "We'll tailor a suggested daily goal tailored to your lifestyle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Weight Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Body Weight", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
            Text(
                text = "${weightKg.toInt()} kg",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SipAquaPrimary)
            )
        }
        Slider(
            value = weightKg,
            onValueChange = onWeightChange,
            valueRange = 40f..140f,
            steps = 99,
            colors = SliderDefaults.colors(thumbColor = SipAquaPrimary, activeTrackColor = SipAquaPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Activity Level
        Text(text = "Daily Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityLevel.values().forEach { level ->
                Surface(
                    onClick = { onActivityChange(level) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (activityLevel == level) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = if (activityLevel == level) null else CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = level.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (activityLevel == level) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (activityLevel == level) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Climate
        Text(text = "Climate", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Climate.values().forEach { c ->
                FilterChip(
                    selected = climate == c,
                    onClick = { onClimateChange(c) },
                    label = { Text(text = c.displayName.split(" ").first(), fontSize = 11.sp) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Suggested Target Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Recommended Daily Target",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$calculatedGoal ml / day",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SipAquaPrimary
                    )
                )
                Text(
                    text = "Calculated as 33 ml/kg with activity & climate adjustments. You can adjust this anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OnboardingStepReminders(
    reminderMode: ReminderMode,
    onReminderModeChange: (ReminderMode) -> Unit,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Gentle Reminders",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "Never feel pressured. Sip notifies you calmly and respects your focus.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Permission card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (permissionGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (permissionGranted) SipAquaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (permissionGranted) Icons.Rounded.Check else Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = if (permissionGranted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (permissionGranted) "Notifications Enabled" else "Allow Notifications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (permissionGranted) "You'll receive subtle pacing nudges" else "Tap to grant reminder permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!permissionGranted) {
                    Button(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Allow")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Reminder Style",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(modifier = Modifier.height(12.dp))

        ReminderMode.values().forEach { mode ->
            val isSelected = reminderMode == mode
            Surface(
                onClick = { onReminderModeChange(mode) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mode.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
