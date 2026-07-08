package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = ElectricCyan,
    tertiary = ElectricAmber,
    background = CosmicBackground,
    surface = CosmicSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    surfaceVariant = CosmicSurface,
    outline = CosmicBorder,
    outlineVariant = CosmicBorder.copy(alpha = 0.5f),
    surfaceContainerLowest = CosmicBackground,
    surfaceContainerLow = CosmicSurface,
    surfaceContainer = CosmicSurface,
    surfaceContainerHigh = CosmicSurface,
    surfaceContainerHighest = CosmicBorder
)

private val MagicOSLightColorScheme = lightColorScheme(
    primary = Color(0xFF0066FF), // MagicOS 11 Royal Blue
    secondary = Color(0xFF4CAF50), // Dialer Green
    tertiary = Color(0xFFFF9800), // Accent Orange
    background = Color(0xFFF5F7FA), // Light grey/blue background
    surface = Color(0xFFFFFFFF),    // Pure white cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF191D24), // Elegant deep charcoal
    onSurface = Color(0xFF191D24),    // Elegant deep charcoal
    onSurfaceVariant = Color(0xFF8A95A5), // Cool slate grey
    surfaceVariant = Color(0xFFEFF2F6), // Light blue-grey search container
    outline = Color(0xFFE2E7ED),        // Very subtle borders
    outlineVariant = Color(0xFFEFF2F6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CosmicColorScheme else MagicOSLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
