package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SipAquaPrimary
import com.example.ui.theme.SipMintAccent
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class DropletParticle(
    val startX: Float,
    val angle: Double,
    val speed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun CelebrationOverlay(
    streakDays: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val colors = listOf(
            SipAquaPrimary,
            SipMintAccent,
            Color(0xFF5CC0F5),
            Color(0xFFBCE6FE),
            Color(0xFF80E5FF)
        )
        List(40) {
            DropletParticle(
                startX = 0.5f + (Random.nextFloat() - 0.5f) * 0.4f,
                angle = Math.PI * 0.8 + Random.nextDouble() * Math.PI * 1.4, // upward burst
                speed = 280f + Random.nextFloat() * 450f,
                size = 6f + Random.nextFloat() * 10f,
                color = colors[it % colors.size]
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2600, easing = LinearEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .testTag("celebration_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Falling water droplet confetti
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = animProgress.value

            particles.forEach { p ->
                // Physics: start from center, burst upward, gravity pulls down
                val vx = cos(p.angle).toFloat() * p.speed
                val vy = sin(p.angle).toFloat() * p.speed
                val gravity = 550f

                val x = (w * p.startX) + (vx * t)
                val y = (h * 0.45f) + (vy * t) + (0.5f * gravity * t * t)
                val alpha = (1f - (t * 0.85f)).coerceIn(0f, 1f)

                if (y in 0f..h && x in 0f..w) {
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * (1f - t * 0.3f),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Congratulatory Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💧",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Goal Smashed!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SipAquaPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You reached your daily hydration target. Your body, mind, and cells thank you!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Streak banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$streakDays Day Streak",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SipAquaPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("celebration_continue_button")
                ) {
                    Text(
                        text = "Awesome!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
