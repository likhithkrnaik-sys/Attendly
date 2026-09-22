package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AttendlyDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF004D68),
    onPrimaryContainer = Color(0xFFC0E8FF),
    secondary = SoftViolet,
    onSecondary = Color(0xFF1C224E),
    secondaryContainer = Color(0xFF303B72),
    onSecondaryContainer = Color(0xFFDFE0FF),
    tertiary = AttendanceSafe,
    onTertiary = Color(0xFF003822),
    background = BrandBackground,
    onBackground = TextPrimary,
    surface = BrandSurface,
    onSurface = TextPrimary,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BrandBorder,
    outlineVariant = BrandBorderSubtle
)

private val AttendlyLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF3730A3),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun AttendlyTheme(
    darkTheme: Boolean = true, // Dark mode first design as per requirement
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AttendlyDarkColorScheme else AttendlyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    AttendlyTheme(darkTheme = darkTheme, content = content)
}
