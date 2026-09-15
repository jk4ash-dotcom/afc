package com.afcpoc.prayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = SoftIvory,
    primaryContainer = Color(0xFFD8E0EC),
    onPrimaryContainer = Navy,
    secondary = SoftRose,
    onSecondary = SoftIvory,
    secondaryContainer = Color(0xFFF0E4E4),
    onSecondaryContainer = SoftRose,
    tertiary = GentleGold,
    onTertiary = TextPrimary,
    background = WarmCream,
    onBackground = TextPrimary,
    surface = SoftIvory,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE8E2D8),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFB0A89C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C7DE),
    onPrimary = Color(0xFF1A2740),
    primaryContainer = SoftNavy,
    onPrimaryContainer = SoftIvory,
    secondary = Color(0xFFD4B8B8),
    onSecondary = Color(0xFF3A2828),
    background = Color(0xFF1A1F28),
    onBackground = SoftIvory,
    surface = Color(0xFF232933),
    onSurface = SoftIvory,
    surfaceVariant = Color(0xFF343B48),
    onSurfaceVariant = Color(0xFFC8CED8)
)

@Composable
fun AfcPrayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
