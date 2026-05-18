package com.gramasethu.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Bridge Status Colors
val GreenOpen = Color(0xFF4CAF50)
val YellowDamaged = Color(0xFFFFC107)
val RedSubmerged = Color(0xFFF44336)
val DarkGreen = Color(0xFF1B5E20)
val MediumGreen = Color(0xFF2E7D32)

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    secondary = MediumGreen,
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    error = RedSubmerged,
    surfaceVariant = Color(0xFFEEEEEE),
    outline = Color(0xFFBDBDBD)
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenOpen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1B5E20),
    secondary = MediumGreen,
    onSecondary = Color.White,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    onBackground = Color(0xFFF0F0F0),
    onSurface = Color(0xFFF0F0F0),
    error = Color(0xFFEF5350),
    surfaceVariant = Color(0xFF2A2A2A),
    outline = Color(0xFF424242)
)

// Custom Typography for the app
val GramaSethuTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
)

@Composable
fun GramaSethuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GramaSethuTypography,
        content = content
    )
}