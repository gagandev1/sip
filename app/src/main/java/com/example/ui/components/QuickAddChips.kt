package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrinkType
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.SipAquaPrimary

@Composable
fun QuickAddChips(
    onAdd: (Int, DrinkType) -> Unit,
    onOpenCustom: () -> Unit,
    glassOpacity: Float = 0.65f,
    reduceTransparency: Boolean = false,
    modifier: Modifier = Modifier
) {
    val presets = listOf(100, 200, 250, 330, 500)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        presets.forEach { ml ->
            GlassSurface(
                shape = RoundedCornerShape(22.dp),
                glassOpacity = glassOpacity,
                reduceTransparency = reduceTransparency,
                elevation = 3.dp,
                onClick = { onAdd(ml, DrinkType.WATER) },
                modifier = Modifier
                    .height(44.dp)
                    .testTag("quick_add_chip_$ml")
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add $ml ml",
                        tint = SipAquaPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$ml ml",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Custom "⋯" pill
        GlassSurface(
            shape = RoundedCornerShape(22.dp),
            glassOpacity = (glassOpacity + 0.15f).coerceAtMost(0.95f),
            reduceTransparency = reduceTransparency,
            elevation = 4.dp,
            onClick = onOpenCustom,
            modifier = Modifier
                .height(44.dp)
                .testTag("quick_add_custom_chip")
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Custom drink",
                    tint = SipAquaPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Custom",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SipAquaPrimary
                    )
                )
            }
        }
    }
}
