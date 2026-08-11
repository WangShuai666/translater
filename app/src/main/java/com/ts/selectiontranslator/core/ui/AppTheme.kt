package com.ts.selectiontranslator.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val GardenTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    ),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF527A5A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDCE8D8),
        onPrimaryContainer = Color(0xFF20352B),
        secondary = Color(0xFF8FB996),
        onSecondary = Color(0xFF20352B),
        secondaryContainer = Color(0xFFE8F0E6),
        onSecondaryContainer = Color(0xFF20352B),
        tertiary = Color(0xFFD96C71),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFCE8E9),
        onTertiaryContainer = Color(0xFF6E2A2E),
        background = Color(0xFFF3F5EE),
        onBackground = Color(0xFF20352B),
        surface = Color.White,
        onSurface = Color(0xFF20352B),
        surfaceVariant = Color(0xFFE6EDE3),
        onSurfaceVariant = Color(0xFF4A5A4D),
        outline = Color(0xFFB9C7B8),
        error = Color(0xFFB3261E),
    )
    val darkColors = darkColorScheme(
        primary = Color(0xFF8FB996),
        onPrimary = Color(0xFF102018),
        primaryContainer = Color(0xFF24432F),
        onPrimaryContainer = Color(0xFFDCE8D8),
        secondary = Color(0xFFA8C2AD),
        onSecondary = Color(0xFF102018),
        secondaryContainer = Color(0xFF2B4633),
        onSecondaryContainer = Color(0xFFDCE8D8),
        tertiary = Color(0xFFE8A1A5),
        onTertiary = Color(0xFF3A1619),
        tertiaryContainer = Color(0xFF57272B),
        onTertiaryContainer = Color(0xFFFCE8E9),
        background = Color(0xFF101A14),
        onBackground = Color(0xFFE4EDE5),
        surface = Color(0xFF152119),
        onSurface = Color(0xFFE4EDE5),
        surfaceVariant = Color(0xFF25352B),
        onSurfaceVariant = Color(0xFFAFC2B3),
        outline = Color(0xFF43574A),
        error = Color(0xFFF2B8B5),
    )

    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColors else lightColors,
        typography = GardenTypography,
        content = content,
    )
}
