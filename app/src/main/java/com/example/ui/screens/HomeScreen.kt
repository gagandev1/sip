package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrinkType
import com.example.model.WaterLogEntry
import com.example.ui.components.CustomAddBottomSheet
import com.example.ui.components.LiquidGlass
import com.example.ui.components.QuickAddChips
import com.example.ui.components.glass.DailyTipChip
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.SipAquaPrimary
import com.example.viewmodel.SipViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SipViewModel,
    tiltFactor: Float,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val isPouring by viewModel.isPouring.collectAsState()
    val waterLogs by viewModel.waterLogs.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()

    var showCustomSheet by remember { mutableStateOf(false) }

    val todayLogs = remember(waterLogs) { viewModel.getTodayLogs() }
    val todayIntake = remember(waterLogs) { viewModel.getTodayIntakeMl() }
    val goal = profile.dailyGoalMl

    // Time-based greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Peaceful night"
        }
    }

    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
    ) {
        // Minimal Greeting + Daily Tip Glass Chip
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Rotating tip glass chip
                DailyTipChip(
                    tip = currentTip,
                    onNextTip = { viewModel.nextTip() },
                    glassOpacity = profile.glassOpacity,
                    reduceTransparency = profile.reduceTransparency
                )
            }
        }

        // Hero Liquid Glass Tumbler
        item {
            LiquidGlass(
                currentMl = todayIntake,
                goalMl = goal,
                tiltFactor = tiltFactor,
                isPouring = isPouring,
                reduceMotion = profile.reduceMotion,
                onLongPress = {
                    viewModel.undoLastEntry()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(vertical = 4.dp)
            )
        }

        // Floating Quick Add Glass Pills
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                QuickAddChips(
                    onAdd = { amount, drinkType ->
                        viewModel.addWater(amount, drinkType)
                    },
                    onOpenCustom = {
                        showCustomSheet = true
                    },
                    glassOpacity = profile.glassOpacity,
                    reduceTransparency = profile.reduceTransparency
                )
            }
        }

        // Today's Timeline Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Intake",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )
                )
                Text(
                    text = "${todayLogs.size} logs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Today's log as light borderless glass list with swipe-to-delete
        if (todayLogs.isEmpty()) {
            item {
                GlassSurface(
                    shape = RoundedCornerShape(22.dp),
                    glassOpacity = profile.glassOpacity,
                    reduceTransparency = profile.reduceTransparency,
                    elevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(text = "🌊", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No drinks logged yet today",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Tap a glass pill above to start your flow",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayLogs, key = { it.id }) { log ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.deleteEntry(log.id)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                                .padding(end = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    GlassSurface(
                        shape = RoundedCornerShape(20.dp),
                        glassOpacity = profile.glassOpacity,
                        reduceTransparency = profile.reduceTransparency,
                        elevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .testTag("water_log_item_${log.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SipAquaPrimary.copy(alpha = 0.16f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = log.drinkType.iconEmoji, fontSize = 18.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+${log.amountMl} ml",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                    )
                                    if (log.drinkType != DrinkType.WATER) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${log.effectiveHydrationMl} ml net)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SipAquaPrimary)
                                        )
                                    }
                                }
                                Text(
                                    text = "${log.drinkType.displayName} • ${timeFormatter.format(Date(log.timestamp))}${if (log.note.isNotBlank()) " • ${log.note}" else ""}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomSheet) {
        CustomAddBottomSheet(
            onDismiss = { showCustomSheet = false },
            onConfirm = { amount, drinkType, note ->
                viewModel.addWater(amount, drinkType, note)
                showCustomSheet = false
            }
        )
    }
}
