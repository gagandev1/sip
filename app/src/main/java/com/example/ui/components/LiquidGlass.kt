package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

private data class GlassBubble(
    val relX: Float,
    val size: Float,
    val speed: Float,
    val wobbleFreq: Float,
    val seed: Float
)

@Composable
fun LiquidGlass(
    currentMl: Int,
    goalMl: Int,
    tiltFactor: Float, // -1f to 1f
    isPouring: Boolean,
    reduceMotion: Boolean = false,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val rawProgress = (currentMl.toFloat() / goalMl.coerceAtLeast(1).toFloat())
    val isOverGoal = currentMl >= goalMl
    val extraMl = (currentMl - goalMl).coerceAtLeast(0)

    // Smooth animated level transition with gentle spring/overshoot
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1.25f),
        animationSpec = if (reduceMotion) tween(300) else spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "LiquidLevel"
    )

    // Animated number counter for percentage
    val percentageInt = (rawProgress * 100f).roundToInt()
    val animatedPercentage by animateIntAsState(
        targetValue = percentageInt,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "NumberRoll"
    )

    // Waves infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "Waves")
    val wavePhase1 by if (reduceMotion) remember { mutableFloatStateOf(0f) } else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase1"
    )
    val wavePhase2 by if (reduceMotion) remember { mutableFloatStateOf(1f) } else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase2"
    )
    val idleSway by if (reduceMotion) remember { mutableFloatStateOf(0f) } else infiniteTransition.animateFloat(
        initialValue = -0.05f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleSway"
    )

    val causticPhase by if (reduceMotion) remember { mutableFloatStateOf(0f) } else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CausticPhase"
    )

    // Pour stream animation
    val pourStreamAlpha by animateFloatAsState(
        targetValue = if (isPouring) 0.9f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "PourStream"
    )

    // Rising micro-bubbles
    val bubbles = remember {
        List(18) {
            GlassBubble(
                relX = 0.12f + Random.nextFloat() * 0.76f,
                size = 2.5f + Random.nextFloat() * 5.5f,
                speed = 0.35f + Random.nextFloat() * 0.65f,
                wobbleFreq = 2.5f + Random.nextFloat() * 3.5f,
                seed = Random.nextFloat() * 10f
            )
        }
    }

    val bubbleTime by if (reduceMotion) remember { mutableFloatStateOf(0f) } else infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BubbleTime"
    )

    val effectiveTilt = if (reduceMotion) 0f else (tiltFactor + idleSway).coerceIn(-0.55f, 0.55f)

    Box(
        modifier = modifier
            .testTag("liquid_glass_hero")
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Tall elegant drinking glass proportions (subtly tapered, curved optical base)
            val topWidth = width * 0.62f
            val bottomWidth = width * 0.52f
            val glassHeight = height * 0.85f

            val glassLeftTop = (width - topWidth) / 2f
            val glassRightTop = glassLeftTop + topWidth
            val glassLeftBottom = (width - bottomWidth) / 2f
            val glassRightBottom = glassLeftBottom + bottomWidth
            val glassTop = height * 0.06f
            val glassBottom = glassTop + glassHeight
            val cornerRadius = 26.dp.toPx()

            // 1. Tabletop Reflection & Soft Aqua Ambient Shadow
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isOverGoal) SipMintAccent.copy(alpha = 0.35f) else SipAquaPrimary.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(width / 2f, glassBottom + 12.dp.toPx()),
                    radius = bottomWidth * 0.65f
                ),
                topLeft = Offset((width - bottomWidth * 1.15f) / 2f, glassBottom - 4.dp.toPx()),
                size = Size(bottomWidth * 1.15f, 26.dp.toPx())
            )

            // 2. Tall Glass Outline Path
            val glassPath = Path().apply {
                moveTo(glassLeftTop, glassTop)
                lineTo(glassRightTop, glassTop)
                lineTo(glassRightBottom, glassBottom - cornerRadius)
                quadraticBezierTo(
                    glassRightBottom, glassBottom,
                    glassRightBottom - cornerRadius, glassBottom
                )
                lineTo(glassLeftBottom + cornerRadius, glassBottom)
                quadraticBezierTo(
                    glassLeftBottom, glassBottom,
                    glassLeftBottom, glassBottom - cornerRadius
                )
                lineTo(glassLeftTop, glassTop)
                close()
            }

            // 3. Ambient glass refractive body
            drawPath(
                path = glassPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isDark) Color(0x12FFFFFF) else Color(0x35FFFFFF),
                        if (isDark) Color(0x223AA7E8) else Color(0x40BCE6FE)
                    ),
                    startY = glassTop,
                    endY = glassBottom
                )
            )

            // 4. Liquid Inside Glass
            clipPath(glassPath) {
                if (animatedProgress > 0.008f) {
                    val fillPercentClamped = animatedProgress.coerceIn(0.01f, 1.0f)
                    val liquidHeight = (glassHeight - 20.dp.toPx()) * fillPercentClamped
                    val liquidSurfaceY = glassBottom - liquidHeight

                    val waveAmp1 = 5.dp.toPx() * (1f + if (isPouring) 0.9f else 0f)
                    val waveAmp2 = 3.dp.toPx()
                    val tiltDelta = effectiveTilt * 24.dp.toPx()

                    // Back Wave Path (layered depth)
                    val backWavePath = Path().apply {
                        moveTo(0f, height)
                        lineTo(0f, liquidSurfaceY - tiltDelta)
                        val steps = 24
                        for (i in 0..steps) {
                            val x = width * (i.toFloat() / steps)
                            val rx = x / width
                            val currentTilt = (rx - 0.5f) * tiltDelta * 2f
                            val y = liquidSurfaceY + currentTilt +
                                    waveAmp2 * sin((rx * 3f * Math.PI.toFloat()) + wavePhase2)
                            lineTo(x, y)
                        }
                        lineTo(width, height)
                        close()
                    }

                    drawPath(
                        path = backWavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x703AA7E8),
                                Color(0xDD114068)
                            ),
                            startY = liquidSurfaceY - 14.dp.toPx(),
                            endY = glassBottom
                        )
                    )

                    // Front Wave Path
                    val frontWavePath = Path().apply {
                        moveTo(0f, height)
                        lineTo(0f, liquidSurfaceY - tiltDelta)
                        val steps = 28
                        for (i in 0..steps) {
                            val x = width * (i.toFloat() / steps)
                            val rx = x / width
                            val currentTilt = (rx - 0.5f) * tiltDelta * 2f
                            val y = liquidSurfaceY + currentTilt +
                                    waveAmp1 * sin((rx * 2.4f * Math.PI.toFloat()) + wavePhase1)
                            lineTo(x, y)
                        }
                        lineTo(width, height)
                        close()
                    }

                    // Main Liquid Gradient
                    val liquidTopColor = if (isOverGoal) SipMintAccent.copy(alpha = 0.92f) else Color(0xFF63EBFF).copy(alpha = 0.90f)
                    val liquidMidColor = if (isOverGoal) SipAquaPrimary.copy(alpha = 0.94f) else Color(0xFF28A0EA).copy(alpha = 0.94f)
                    val liquidBotColor = if (isOverGoal) Color(0xFF104A75) else Color(0xFF0D365C)

                    drawPath(
                        path = frontWavePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(liquidTopColor, liquidMidColor, liquidBotColor),
                            startY = liquidSurfaceY - 6.dp.toPx(),
                            endY = glassBottom
                        )
                    )

                    // Bright Meniscus Line at liquid surface
                    val meniscusSteps = 28
                    val meniscusPath = Path()
                    for (i in 0..meniscusSteps) {
                        val x = width * (i.toFloat() / meniscusSteps)
                        val rx = x / width
                        val currentTilt = (rx - 0.5f) * tiltDelta * 2f
                        val y = liquidSurfaceY + currentTilt +
                                waveAmp1 * sin((rx * 2.4f * Math.PI.toFloat()) + wavePhase1)
                        if (i == 0) meniscusPath.moveTo(x, y) else meniscusPath.lineTo(x, y)
                    }
                    drawPath(
                        path = meniscusPath,
                        color = Color(0xF0FFFFFF),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Caustic Light Patterns shifting slowly
                    val causticOffset = sin(causticPhase) * 16.dp.toPx()
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color(0x30FFFFFF), Color.Transparent),
                            start = Offset(glassLeftBottom + causticOffset, liquidSurfaceY),
                            end = Offset(glassRightBottom - causticOffset, glassBottom)
                        ),
                        topLeft = Offset(glassLeftBottom + 12.dp.toPx(), liquidSurfaceY + 14.dp.toPx()),
                        size = Size(bottomWidth * 0.72f, liquidHeight * 0.55f),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )

                    // Rising Micro-bubbles
                    bubbles.forEach { b ->
                        val bProg = ((bubbleTime * b.speed + b.seed) % 1f)
                        val by = glassBottom - (liquidHeight * bProg)
                        if (by > liquidSurfaceY + 6.dp.toPx()) {
                            val wobble = sin(bProg * b.wobbleFreq * 6f) * 6.dp.toPx()
                            val bx = (glassLeftBottom + bottomWidth * b.relX) + wobble
                            drawCircle(
                                color = Color(0xB0FFFFFF),
                                radius = b.size.dp.toPx() / 2f,
                                center = Offset(bx, by)
                            )
                        }
                    }

                    // Pour stream falling into glass
                    if (pourStreamAlpha > 0.01f) {
                        val pourCenterX = width / 2f + effectiveTilt * 14.dp.toPx()
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xE0FFFFFF), Color(0xF03AA7E8)),
                                startY = 0f,
                                endY = liquidSurfaceY
                            ),
                            start = Offset(pourCenterX, 0f),
                            end = Offset(pourCenterX, liquidSurfaceY + 4.dp.toPx()),
                            strokeWidth = 5.5.dp.toPx() * pourStreamAlpha,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 5. Polished Glass Rim (top arc)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xD0FFFFFF), Color(0x40FFFFFF)),
                    center = Offset(width / 2f, glassTop + 3.dp.toPx()),
                    radius = topWidth / 2f
                ),
                topLeft = Offset(glassLeftTop, glassTop - 3.dp.toPx()),
                size = Size(topWidth, 7.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // 6. Curved Glass Highlight Streak (left side)
            val leftStreak = Path().apply {
                moveTo(glassLeftTop + 7.dp.toPx(), glassTop + 12.dp.toPx())
                lineTo(glassLeftBottom + 10.dp.toPx(), glassBottom - 26.dp.toPx())
            }
            drawPath(
                path = leftStreak,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x85FFFFFF), Color(0x10FFFFFF)),
                    startY = glassTop,
                    endY = glassBottom
                ),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 7. Outer Glass Frame with Edge Light
            drawPath(
                path = glassPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xB0FFFFFF),
                        Color(0x453AA7E8),
                        Color(0x75FFFFFF)
                    ),
                    startY = glassTop,
                    endY = glassBottom
                ),
                style = Stroke(width = 2.2.dp.toPx())
            )

            // 8. Thick Optical Glass Bottom
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x28FFFFFF), Color(0x503AA7E8)),
                    startY = glassBottom - 18.dp.toPx(),
                    endY = glassBottom
                ),
                topLeft = Offset(glassLeftBottom + 4.dp.toPx(), glassBottom - 16.dp.toPx()),
                size = Size(bottomWidth - 8.dp.toPx(), 14.dp.toPx()),
                cornerRadius = CornerRadius(cornerRadius * 0.75f)
            )
        }

        // Clean typography sitting directly on the glass (NO frosted box behind it!)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 18.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "$animatedPercentage%",
                style = Typography.displayLarge.copy(
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0x80001B33),
                        offset = Offset(0f, 3f),
                        blurRadius = 8f
                    )
                )
            )

            Text(
                text = "$currentMl / $goalMl ml",
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.94f),
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color(0x60001B33),
                        offset = Offset(0f, 2f),
                        blurRadius = 6f
                    )
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isOverGoal) {
                Text(
                    text = "Goal reached · +$extraMl ml extra",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SipMintAccent,
                        fontWeight = FontWeight.SemiBold,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0x80042D26),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            } else {
                Text(
                    text = "Hold glass to undo",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
