package com.example.ui.components.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadiusDp: Dp = 24.dp,
    elevation: Dp = 6.dp,
    glassOpacity: Float = 0.65f,
    reduceTransparency: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Pressed spring physics
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "GlassScale"
    )

    // Base glass fill
    val baseFill = if (reduceTransparency) {
        if (isDark) Color(0xFF132230) else Color(0xFFF1F6FB)
    } else {
        if (isDark) {
            Color(0xFF0F2233).copy(alpha = (0.35f * (glassOpacity / 0.65f)).coerceIn(0.15f, 0.85f))
        } else {
            Color(0xFFFFFFFF).copy(alpha = (0.62f * (glassOpacity / 0.65f)).coerceIn(0.35f, 0.95f))
        }
    }

    // 1px Edge Light Gradient Border: bright top-left, subtle faded bottom-right
    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x60FFFFFF),
                Color(0x203AA7E8),
                Color(0x08FFFFFF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0x99FFFFFF),
                Color(0x403AA7E8),
                Color(0x20FFFFFF)
            )
        )
    }

    // Soft tinted shadow
    val shadowColor = if (isDark) Color(0x3300182E) else Color(0x181B5E9B)

    val pressModifier = if (onClick != null) {
        Modifier.pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        onClick()
                    }
                }
            }
        }
    } else Modifier

    Box(
        modifier = modifier
            .scale(animatedScale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(baseFill)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(pressModifier)
    ) {
        // Specular sheen overlay (subtle diagonal highlight streak across top)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x18FFFFFF),
                            Color(0x06FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}
