package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = MatrouhPrimary,
    onPrimary = Color.White,
    primaryContainer = MatrouhPrimaryLight,
    onPrimaryContainer = Color.White,
    secondary = MatrouhAccent,
    onSecondary = MatrouhPrimaryDark,
    secondaryContainer = MatrouhAccentLight,
    onSecondaryContainer = MatrouhPrimaryDark,
    background = MatrouhBackground,
    onBackground = TextPrimary,
    surface = MatrouhSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    error = MatrouhRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = MatrouhPrimary,
    onPrimaryContainer = Color.White,
    secondary = MatrouhAccent,
    onSecondary = Color.Black,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    error = Color(0xFFF87171),
    onError = Color.Black
)

@Composable
fun MatrouhMallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
