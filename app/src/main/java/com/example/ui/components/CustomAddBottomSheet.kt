package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrinkType
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.SipAquaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAddBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (Int, DrinkType, String) -> Unit
) {
    var selectedDrink by remember { mutableStateOf(DrinkType.WATER) }
    var amountMl by remember { mutableFloatStateOf(250f) }
    var textInput by remember { mutableStateOf("250") }
    var note by remember { mutableStateOf("") }

    val effectiveHydration = (amountMl * selectedDrink.hydrationFactor).toInt()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.testTag("custom_add_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log Drink",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Drink Type Selector Row
            Text(
                text = "Beverage Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DrinkType.values().forEach { drink ->
                    val isSelected = selectedDrink == drink
                    GlassSurface(
                        shape = RoundedCornerShape(20.dp),
                        glassOpacity = if (isSelected) 0.85f else 0.45f,
                        elevation = if (isSelected) 4.dp else 1.dp,
                        onClick = { selectedDrink = drink },
                        modifier = Modifier.testTag("drink_type_${drink.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(if (isSelected) SipAquaPrimary.copy(alpha = 0.25f) else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(text = drink.iconEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = drink.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) SipAquaPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(drink.hydrationFactor * 100).toInt()}% Hydration",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Display & Direct Input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { newStr ->
                        if (newStr.length <= 4 && newStr.all { it.isDigit() }) {
                            textInput = newStr
                            val parsed = newStr.toIntOrNull() ?: 0
                            amountMl = parsed.toFloat().coerceIn(0f, 2000f)
                        }
                    },
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light,
                        color = SipAquaPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .width(160.dp)
                        .testTag("custom_amount_input")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ml",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (selectedDrink != DrinkType.WATER) {
                        Text(
                            text = "= $effectiveHydration ml net",
                            style = MaterialTheme.typography.labelSmall.copy(color = SipAquaPrimary)
                        )
                    }
                }
            }

            // Slider with liquid color
            Slider(
                value = amountMl,
                onValueChange = { newVal ->
                    val rounded = ((newVal / 25).toInt() * 25).toFloat()
                    amountMl = rounded
                    textInput = rounded.toInt().toString()
                },
                valueRange = 50f..1200f,
                steps = 45,
                colors = SliderDefaults.colors(
                    thumbColor = SipAquaPrimary,
                    activeTrackColor = SipAquaPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("custom_amount_slider")
            )

            // Optional note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                placeholder = { Text("e.g., Post-workout hydration") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Confirm Button
            Button(
                onClick = {
                    val finalAmount = textInput.toIntOrNull() ?: amountMl.toInt()
                    if (finalAmount > 0) {
                        onConfirm(finalAmount, selectedDrink, note)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SipAquaPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_custom_add_button")
            ) {
                Text(
                    text = "Add ${textInput.ifEmpty { "0" }} ml ${selectedDrink.displayName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
