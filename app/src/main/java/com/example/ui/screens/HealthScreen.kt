package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyCheckIn
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.SipAquaPrimary
import com.example.ui.theme.SipMintAccent
import com.example.viewmodel.SipViewModel

@Composable
fun HealthScreen(
    viewModel: SipViewModel,
    modifier: Modifier = Modifier
) {
    val checkIns by viewModel.checkIns.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val insights = remember(checkIns) { viewModel.getHealthInsights() }

    val todayStr = remember { viewModel.getTodayString() }
    val todayEntry = remember(checkIns) { viewModel.getTodayCheckIn() }

    var mood by remember(todayEntry) { mutableIntStateOf(todayEntry?.mood ?: 3) }
    var energy by remember(todayEntry) { mutableIntStateOf(todayEntry?.energy ?: 3) }
    var sleepQuality by remember(todayEntry) { mutableIntStateOf(todayEntry?.sleepQuality ?: 3) }
    var hoursSlept by remember(todayEntry) { mutableFloatStateOf(todayEntry?.hoursSlept ?: 7.5f) }
    var currentWeight by remember(todayEntry, profile) {
        mutableFloatStateOf(todayEntry?.weightKg ?: profile.weightKg)
    }
    var selectedSymptoms by remember(todayEntry) {
        mutableStateOf(todayEntry?.symptoms?.toSet() ?: emptySet())
    }

    val availableSymptoms = remember {
        listOf(
            "Clear skin", "High focus", "Energetic",
            "Headache", "Fatigue", "Dry skin", "Muscle cramps", "Brain fog"
        )
    }

    val heightM = profile.heightCm / 100f
    val bmi = remember(currentWeight, heightM) {
        if (heightM > 0f) currentWeight / (heightM * heightM) else 22f
    }
    val bmiCategory = when {
        bmi < 18.5f -> "Lean range"
        bmi < 25f -> "Standard range"
        bmi < 30f -> "Elevated range"
        else -> "High range"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("health_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Vitality",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "Daily check-in and personal wellness trends",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Daily Check-in Card (Liquid Glass Surface)
        item {
            GlassSurface(
                shape = RoundedCornerShape(28.dp),
                glassOpacity = profile.glassOpacity,
                reduceTransparency = profile.reduceTransparency,
                elevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Rhythm",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = todayStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = SipAquaPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mood selector
                    Text(
                        text = "Mood State",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val moods = listOf("😞", "😕", "😐", "🙂", "😄")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        moods.forEachIndexed { index, emoji ->
                            val score = index + 1
                            val isSelected = mood == score
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SipAquaPrimary.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable {
                                        mood = score
                                        viewModel.saveCheckIn(
                                            DailyCheckIn(
                                                dateString = todayStr,
                                                mood = mood,
                                                energy = energy,
                                                sleepQuality = sleepQuality,
                                                hoursSlept = hoursSlept,
                                                weightKg = currentWeight,
                                                symptoms = selectedSymptoms.toList()
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Energy slider with liquid track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Energy",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$energy / 5",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SipAquaPrimary)
                        )
                    }
                    Slider(
                        value = energy.toFloat(),
                        onValueChange = {
                            energy = it.toInt()
                            viewModel.saveCheckIn(
                                DailyCheckIn(
                                    dateString = todayStr,
                                    mood = mood,
                                    energy = energy,
                                    sleepQuality = sleepQuality,
                                    hoursSlept = hoursSlept,
                                    weightKg = currentWeight,
                                    symptoms = selectedSymptoms.toList()
                                )
                            )
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = SipAquaPrimary, activeTrackColor = SipAquaPrimary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sleep Duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sleep Rest",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f hrs", hoursSlept),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SipMintAccent)
                        )
                    }
                    Slider(
                        value = hoursSlept,
                        onValueChange = {
                            hoursSlept = Math.round(it * 2f) / 2f
                            viewModel.saveCheckIn(
                                DailyCheckIn(
                                    dateString = todayStr,
                                    mood = mood,
                                    energy = energy,
                                    sleepQuality = sleepQuality,
                                    hoursSlept = hoursSlept,
                                    weightKg = currentWeight,
                                    symptoms = selectedSymptoms.toList()
                                )
                            )
                        },
                        valueRange = 4f..12f,
                        steps = 15,
                        colors = SliderDefaults.colors(thumbColor = SipMintAccent, activeTrackColor = SipMintAccent)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Wellness Notes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OptGroupFlow(
                        items = availableSymptoms,
                        selected = selectedSymptoms,
                        onToggle = { tag ->
                            val updated = if (selectedSymptoms.contains(tag)) {
                                selectedSymptoms - tag
                            } else {
                                selectedSymptoms + tag
                            }
                            selectedSymptoms = updated
                            viewModel.saveCheckIn(
                                DailyCheckIn(
                                    dateString = todayStr,
                                    mood = mood,
                                    energy = energy,
                                    sleepQuality = sleepQuality,
                                    hoursSlept = hoursSlept,
                                    weightKg = currentWeight,
                                    symptoms = updated.toList()
                                )
                            )
                        }
                    )
                }
            }
        }

        // Body Metrics Card (Glass Tile)
        item {
            GlassSurface(
                shape = RoundedCornerShape(28.dp),
                glassOpacity = profile.glassOpacity,
                reduceTransparency = profile.reduceTransparency,
                elevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Body Metrics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Weight",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f kg", currentWeight),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "BMI Ratio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", bmi),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light, color = SipAquaPrimary)
                            )
                            Text(
                                text = bmiCategory,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Historical weight line chart
                    val weightHistory = remember(checkIns) {
                        checkIns.mapNotNull { it.weightKg }.takeLast(7)
                    }

                    if (weightHistory.size >= 2) {
                        val minW = weightHistory.minOrNull() ?: 60f
                        val maxW = (weightHistory.maxOrNull() ?: 80f) + 0.5f

                        val strokeColor = SipAquaPrimary
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val path = Path()
                            val stepX = w / (weightHistory.size - 1)

                            weightHistory.forEachIndexed { i, wt ->
                                val x = i * stepX
                                val y = h - ((wt - minW) / (maxW - minW).coerceAtLeast(0.1f) * (h - 16.dp.toPx())) - 8.dp.toPx()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                drawCircle(color = strokeColor, radius = 4.dp.toPx(), center = Offset(x, y))
                            }
                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }

        // Personal Trends & Insights (Glass Card)
        item {
            GlassSurface(
                shape = RoundedCornerShape(28.dp),
                glassOpacity = profile.glassOpacity,
                reduceTransparency = profile.reduceTransparency,
                elevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✦", fontSize = 18.sp, color = SipAquaPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Personal Trends & Insights",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SipAquaPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    insights.forEach { insight ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = "• ", color = SipAquaPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                text = insight,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sip is for wellness tracking and is not medical advice.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptGroupFlow(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { tag ->
            val isSelected = selected.contains(tag)
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(tag) },
                label = { Text(text = tag, style = MaterialTheme.typography.labelSmall) },
                shape = RoundedCornerShape(14.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

