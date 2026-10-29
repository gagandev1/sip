package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Achievement
import com.example.model.DrinkType
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.SipAquaPrimary
import com.example.ui.theme.SipMintAccent
import com.example.viewmodel.SipViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class StatsPeriod(val title: String, val days: Int) {
    WEEK("Week", 7),
    MONTH("Month", 30),
    YEAR("Year", 365)
}

@Composable
fun StatsScreen(
    viewModel: SipViewModel,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    val waterLogs by viewModel.waterLogs.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val (currentStreak, longestStreak) = remember(waterLogs) { viewModel.calculateStreak() }
    val dailyAvg = remember(waterLogs) { viewModel.calculateDailyAverageMl() }
    val bestDay = remember(waterLogs) { viewModel.calculateBestDayMl() }
    val drinkBreakdown = remember(waterLogs) { viewModel.getDrinkTypeBreakdown() }

    val goal = profile.dailyGoalMl
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayLabelFormat = remember { SimpleDateFormat("EEE", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Minimal Trends Header
        item {
            Column {
                Text(
                    text = "Trends",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "Your fluid rhythm and hydration milestones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Segmented Control as seamless Glass Pill
        item {
            GlassSurface(
                shape = RoundedCornerShape(26.dp),
                glassOpacity = profile.glassOpacity,
                reduceTransparency = profile.reduceTransparency,
                elevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatsPeriod.values().forEach { period ->
                        val isSelected = selectedPeriod == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SipAquaPrimary.copy(alpha = 0.28f) else Color.Transparent)
                                .clickable {
                                    selectedPeriod = period
                                    selectedBarIndex = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) SipAquaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }

        // Liquid Columns Daily Intake Chart (Borderless glass tile)
        item {
            val barData = remember(waterLogs, selectedPeriod) {
                val numDays = if (selectedPeriod == StatsPeriod.WEEK) 7 else 14
                val cal = Calendar.getInstance()
                val result = mutableListOf<Triple<String, Int, String>>()
                for (i in (numDays - 1) downTo 0) {
                    cal.time = Date()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    val key = dateFormat.format(cal.time)
                    val label = if (numDays == 7) dayLabelFormat.format(cal.time) else cal.get(Calendar.DAY_OF_MONTH).toString()
                    val sum = waterLogs.filter { dateFormat.format(Date(it.timestamp)) == key }
                        .sumOf { it.effectiveHydrationMl }
                    result.add(Triple(label, sum, key))
                }
                result
            }

            val maxIntake = remember(barData, goal) {
                (barData.maxOfOrNull { it.second } ?: goal).coerceAtLeast(goal + 400)
            }

            GlassSurface(
                shape = RoundedCornerShape(28.dp),
                glassOpacity = profile.glassOpacity,
                reduceTransparency = profile.reduceTransparency,
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intake History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        if (selectedBarIndex != null && selectedBarIndex!! < barData.size) {
                            val selected = barData[selectedBarIndex!!]
                            Text(
                                text = "${selected.first}: ${selected.second} ml",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SipAquaPrimary
                                )
                            )
                        } else {
                            Text(
                                text = "Goal: $goal ml",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clickable {
                                // Clear selection on canvas tap
                                selectedBarIndex = null
                            }
                    ) {
                        val w = size.width
                        val h = size.height - 24.dp.toPx()
                        val barCount = barData.size
                        val barWidth = (w / barCount) * 0.52f
                        val slotWidth = w / barCount

                        // Glowing thin dashed goal line
                        val goalY = h - (h * (goal.toFloat() / maxIntake.toFloat()))
                        drawLine(
                            color = SipMintAccent.copy(alpha = 0.85f),
                            start = Offset(0f, goalY),
                            end = Offset(w, goalY),
                            strokeWidth = 1.8.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )

                        // Draw liquid column bars
                        barData.forEachIndexed { index, (_, intake, _) ->
                            val centerX = (index * slotWidth) + (slotWidth / 2f)
                            val barHeight = (h * (intake.toFloat() / maxIntake.toFloat())).coerceIn(0f, h)
                            val barTop = h - barHeight
                            val isGoalMet = intake >= goal
                            val isSelected = selectedBarIndex == index

                            val barGradient = Brush.verticalGradient(
                                colors = listOf(
                                    if (isGoalMet) SipMintAccent.copy(alpha = 0.95f) else SipAquaPrimary.copy(alpha = 0.90f),
                                    if (isGoalMet) SipAquaPrimary.copy(alpha = 0.80f) else Color(0xFF1B5E9B).copy(alpha = 0.70f)
                                ),
                                startY = barTop,
                                endY = h
                            )

                            // Liquid column
                            drawRoundRect(
                                brush = barGradient,
                                topLeft = Offset(centerX - barWidth / 2f, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )

                            // Inner top meniscus highlight on each bar
                            if (barHeight > 4.dp.toPx()) {
                                drawLine(
                                    color = Color(0xD0FFFFFF),
                                    start = Offset(centerX - barWidth / 3f, barTop + 2.dp.toPx()),
                                    end = Offset(centerX + barWidth / 3f, barTop + 2.dp.toPx()),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            }
                        }
                    }

                    // Bar labels row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        barData.forEachIndexed { index, (label, _, _) ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedBarIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (selectedBarIndex == index) SipAquaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { selectedBarIndex = index }
                            )
                        }
                    }
                }
            }
        }

        // Summary Metric Tiles (Frosted Glass Tiles with soft glow)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassStatTile(
                        title = "Current Streak",
                        value = "$currentStreak Days",
                        subtitle = "Target: $goal ml/day",
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconTint = Color(0xFFFF7043),
                        modifier = Modifier.weight(1f)
                    )
                    GlassStatTile(
                        title = "Longest Streak",
                        value = "$longestStreak Days",
                        subtitle = "Personal record",
                        icon = Icons.Rounded.EmojiEvents,
                        iconTint = Color(0xFFFFA000),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassStatTile(
                        title = "Daily Average",
                        value = "$dailyAvg ml",
                        subtitle = "${if (dailyAvg >= goal) "Above" else "Below"} goal",
                        icon = Icons.Rounded.WaterDrop,
                        iconTint = SipAquaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    GlassStatTile(
                        title = "Best Day",
                        value = "$bestDay ml",
                        subtitle = "Hydration peak",
                        icon = Icons.Rounded.TrendingUp,
                        iconTint = SipMintAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Calendar Heatmap Restyled in Glass
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
                        text = "Consistency Heatmap",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Color intensity reflects goal percentage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val daysGrid = remember(waterLogs, goal) {
                        val cal = Calendar.getInstance()
                        val list = mutableListOf<Float>()
                        for (i in 27 downTo 0) {
                            cal.time = Date()
                            cal.add(Calendar.DAY_OF_YEAR, -i)
                            val key = dateFormat.format(cal.time)
                            val intake = waterLogs.filter { dateFormat.format(Date(it.timestamp)) == key }
                                .sumOf { it.effectiveHydrationMl }
                            list.add((intake.toFloat() / goal.toFloat()).coerceIn(0f, 1.2f))
                        }
                        list
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (col in 0 until 7) {
                                    val index = row * 7 + col
                                    val ratio = daysGrid.getOrElse(index) { 0f }
                                    val boxColor = when {
                                        ratio == 0f -> Color.White.copy(alpha = 0.08f)
                                        ratio < 0.4f -> SipAquaPrimary.copy(alpha = 0.30f)
                                        ratio < 0.75f -> SipAquaPrimary.copy(alpha = 0.60f)
                                        ratio < 1.0f -> SipAquaPrimary.copy(alpha = 0.85f)
                                        else -> SipMintAccent.copy(alpha = 0.95f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(boxColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Achievements in Glass Cards
        item {
            Column {
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    achievements.forEach { achievement ->
                        GlassAchievementItem(achievement = achievement)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassStatTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        shape = RoundedCornerShape(22.dp),
        elevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GlassAchievementItem(achievement: Achievement) {
    GlassSurface(
        shape = RoundedCornerShape(20.dp),
        elevation = if (achievement.isUnlocked) 3.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked) SipAquaPrimary.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (achievement.isUnlocked) achievement.iconEmoji else "🔒", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (achievement.isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SipMintAccent.copy(alpha = 0.20f)
                ) {
                    Text(
                        text = "Unlocked",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SipMintAccent
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
