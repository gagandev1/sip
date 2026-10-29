package com.example.ui.components.glass

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidBackground(
    progress: Float, // 0f to 1f+
    themeMode: ThemeMode,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Time-of-day subtle tint
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val timeTint = remember(hour) {
        when (hour) {
            in 5..10 -> Color(0x18FFB74D) // morning warm sun
            in 11..16 -> Color(0x00000000) // clear midday
            in 17..20 -> Color(0x15FF7043) // sunset amber
            else -> Color(0x200B1A2C) // nocturnal deep indigo
        }
    }

    // Continuous smooth animation timers (20s - 36s loops)
    val infiniteTransition = rememberInfiniteTransition(label = "AuroraBlobs")

    val t1 by if (reduceMotion) remember { mutableFloatStateOf(0.4f) } else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(28000, easing = LinearEasing)),
        label = "Blob1"
    )

    val t2 by if (reduceMotion) remember { mutableFloatStateOf(0.8f) } else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(34000, easing = LinearEasing)),
        label = "Blob2"
    )

    val t3 by if (reduceMotion) remember { mutableFloatStateOf(1.2f) } else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(22000, easing = LinearEasing)),
        label = "Blob3"
    )

    // Palette tokens
    val baseGradient = if (isDark) {
        listOf(
            Color(0xFF04121F),
            Color(0xFF0A2A43),
            Color(0xFF0E4A6B)
        )
    } else {
        listOf(
            Color(0xFFF3F8FC),
            Color(0xFFE5F1FA),
            Color(0xFFD3EAF7)
        )
    }

    // Aurora colors
    val blobAqua = if (isDark) Color(0xFF3AD6F2) else Color(0xFF6EDFF6)
    val blobTeal = if (isDark) Color(0xFF2FE0C0) else Color(0xFF86EBD8)
    val blobViolet = if (isDark) Color(0xFF7C6CFF) else Color(0xFFA6A0FF)
    val blobCoral = if (isDark) Color(0xFFFF8A7A) else Color(0xFFFFB3A7)

    // Higher intake makes the scene slightly brighter and more luminous
    val intakeBonus = (progress.coerceIn(0f, 1.5f) * 0.15f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Base deep oceanic gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = baseGradient,
                startY = 0f,
                endY = h
            )
        )

        // 2. Blob 1: Aqua (top-right drifting)
        val b1x = w * (0.65f + 0.22f * cos(t1))
        val b1y = h * (0.25f + 0.18f * sin(t1))
        val b1Radius = w * (0.75f + intakeBonus)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blobAqua.copy(alpha = if (isDark) 0.35f + intakeBonus else 0.40f),
                    Color.Transparent
                ),
                center = Offset(b1x, b1y),
                radius = b1Radius
            ),
            radius = b1Radius,
            center = Offset(b1x, b1y)
        )

        // 3. Blob 2: Teal (center-left drifting)
        val b2x = w * (0.30f + 0.25f * sin(t2))
        val b2y = h * (0.55f + 0.20f * cos(t2))
        val b2Radius = w * (0.80f + intakeBonus)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blobTeal.copy(alpha = if (isDark) 0.28f + intakeBonus else 0.35f),
                    Color.Transparent
                ),
                center = Offset(b2x, b2y),
                radius = b2Radius
            ),
            radius = b2Radius,
            center = Offset(b2x, b2y)
        )

        // 4. Blob 3: Soft Violet (bottom drifting)
        val b3x = w * (0.70f + 0.20f * cos(t3))
        val b3y = h * (0.80f + 0.15f * sin(t3))
        val b3Radius = w * 0.85f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blobViolet.copy(alpha = if (isDark) 0.22f else 0.25f),
                    Color.Transparent
                ),
                center = Offset(b3x, b3y),
                radius = b3Radius
            ),
            radius = b3Radius,
            center = Offset(b3x, b3y)
        )

        // 5. Blob 4: Tiny warm coral accent (top-left subtle sunrise flare)
        val b4x = w * (0.20f + 0.12f * cos(t1 + 1f))
        val b4y = h * (0.15f + 0.12f * sin(t2 + 1f))
        val b4Radius = w * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    blobCoral.copy(alpha = if (isDark) 0.15f else 0.18f),
                    Color.Transparent
                ),
                center = Offset(b4x, b4y),
                radius = b4Radius
            ),
            radius = b4Radius,
            center = Offset(b4x, b4y)
        )

        // 6. Time of day tint overlay
        if (timeTint.alpha > 0f) {
            drawRect(color = timeTint)
        }
    }
}
