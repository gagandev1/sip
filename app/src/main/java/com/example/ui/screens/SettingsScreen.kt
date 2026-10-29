package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.SipAquaPrimary
import com.example.viewmodel.SipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SipViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "Customize your glass interface, targets, and sensory feedback",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Appearance & Liquid Glass Section
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
                        text = "Appearance & Glass",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme selector (Dark / Light / Auto)
                    Text(
                        text = "Theme Palette",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { theme ->
                            val isSelected = profile.themeMode == theme
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) SipAquaPrimary.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f))
                                    .clickable { viewModel.updateProfile(profile.copy(themeMode = theme)) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = theme.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) SipAquaPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Glass Intensity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Glass Intensity",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${(profile.glassOpacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SipAquaPrimary)
                        )
                    }
                    Slider(
                        value = profile.glassOpacity,
                        onValueChange = { viewModel.updateProfile(profile.copy(glassOpacity = it)) },
                        valueRange = 0.3f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = SipAquaPrimary, activeTrackColor = SipAquaPrimary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reduce Motion Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduce Motion", style = MaterialTheme.typography.bodyMedium)
                            Text("Replaces slosh and fluid animations with simple fades", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = profile.reduceMotion,
                            onCheckedChange = { viewModel.updateProfile(profile.copy(reduceMotion = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reduce Transparency Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduce Transparency", style = MaterialTheme.typography.bodyMedium)
                            Text("High contrast blur-free solid glass surfaces", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = profile.reduceTransparency,
                            onCheckedChange = { viewModel.updateProfile(profile.copy(reduceTransparency = it)) }
                        )
                    }
                }
            }
        }

        // Daily Target Section
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
                        text = "Daily Target",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${profile.dailyGoalMl} ml",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 28.sp,
                                    color = SipAquaPrimary
                                )
                            )
                            Text(
                                text = "Adjusted for ${profile.activityLevel.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showGoalDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SipAquaPrimary)
                        ) {
                            Text("Edit Target", color = Color.White)
                        }
                    }
                }
            }
        }

        // Gentle Reminders Section
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
                        text = "Reminders",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReminderMode.values().forEach { mode ->
                            val isSelected = profile.reminderMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) SipAquaPrimary.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f))
                                    .clickable { viewModel.updateProfile(profile.copy(reminderMode = mode)) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) SipAquaPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Active hours: ${profile.wakeUpTime} to ${profile.bedtime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = profile.reminderMode.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = SipAquaPrimary
                    )
                }
            }
        }

        // Sensory Feedback (Tactile Haptics & Water Drop Sound)
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
                        text = "Sensory Feedback",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tactile Haptics", style = MaterialTheme.typography.bodyMedium)
                            Text("Subtle vibration on sip recorded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = profile.hapticsEnabled,
                            onCheckedChange = { viewModel.updateProfile(profile.copy(hapticsEnabled = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Water Drop Acoustic", style = MaterialTheme.typography.bodyMedium)
                            Text("Soft synthesized droplet tone on add", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = profile.soundEnabled,
                            onCheckedChange = { viewModel.updateProfile(profile.copy(soundEnabled = it)) }
                        )
                    }
                }
            }
        }

        // Data & Management Section
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
                        text = "Data & Storage",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "All logs remain strictly local to your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val json = viewModel.exportDataAsJson()
                                exportedText = json
                                showExportDialog = true
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SipAquaPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.seedSampleData()
                                Toast.makeText(context, "Seeded 7 days of realistic logs", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Seed Sample")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.clearSampleData()
                                Toast.makeText(context, "Cleared logs", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Logs")
                        }

                        Button(
                            onClick = {
                                viewModel.resetAllData()
                                Toast.makeText(context, "Reset all settings & data", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset All")
                        }
                    }
                }
            }
        }
    }

    // Daily Goal Dialog
    if (showGoalDialog) {
        var goalInput by remember { mutableStateOf(profile.dailyGoalMl.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Edit Target Intake") },
            text = {
                Column {
                    Text(
                        "Optimal standard: 30-35 ml per kg of body weight with activity adjustment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) goalInput = it },
                        label = { Text("Daily goal (ml)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newGoal = goalInput.toIntOrNull() ?: profile.dailyGoalMl
                        viewModel.updateProfile(profile.copy(dailyGoalMl = newGoal.coerceIn(500, 6000)))
                        showGoalDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported Data (JSON)") },
            text = {
                Column {
                    Text("Copy your records to store or transfer:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedText,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Sip Export Data", exportedText))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
