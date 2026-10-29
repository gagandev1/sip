package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode

private val SipLightColorScheme = lightColorScheme(
    primary = SipAquaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EFFD),
    onPrimaryContainer = SipDeepBlue,
    secondary = SipDeepBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1EEF8),
    onSecondaryContainer = SipTextLight,
    tertiary = SipMintAccent,
    onTertiary = SipTextLight,
    background = SipBackgroundLight,
    onBackground = SipTextLight,
    surface = SipSurfaceLight,
    onSurface = SipTextLight,
    surfaceVariant = SipSurfaceVariantLight,
    onSurfaceVariant = SipMutedTextLight,
    outline = SipBorderLight
)

private val SipDarkColorScheme = darkColorScheme(
    primary = SipAquaDark,
    onPrimary = Color(0xFF062238),
    primaryContainer = Color(0xFF133852),
    onPrimaryContainer = Color(0xFFBCE6FE),
    secondary = SipDeepBlueDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF193B57),
    onSecondaryContainer = SipTextDark,
    tertiary = SipMintDark,
    onTertiary = Color(0xFF042D26),
    background = SipBackgroundDark,
    onBackground = SipTextDark,
    surface = SipSurfaceDark,
    onSurface = SipTextDark,
    surfaceVariant = SipSurfaceVariantDark,
    onSurfaceVariant = SipMutedTextDark,
    outline = SipBorderDark
)

@Composable
fun SipTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) SipDarkColorScheme else SipLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
