package com.ts.selectiontranslator.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF0F766E),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFCCFBF1),
        onPrimaryContainer = Color(0xFF134E4A),
        secondary = Color(0xFF64748B),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE2E8F0),
        onSecondaryContainer = Color(0xFF1E293B),
        tertiary = Color(0xFF475569),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE2E8F0),
        onTertiaryContainer = Color(0xFF0F172A),
        background = Color(0xFFF8FAFC),
        onBackground = Color(0xFF0F172A),
        surface = Color.White,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFE2E8F0),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFCBD5E1),
    )
    val darkColors = darkColorScheme(
        primary = Color(0xFF5EEAD4),
        onPrimary = Color(0xFF042F2E),
        primaryContainer = Color(0xFF134E4A),
        onPrimaryContainer = Color(0xFFA7F3D0),
        secondary = Color(0xFF94A3B8),
        onSecondary = Color(0xFF0F172A),
        tertiary = Color(0xFF94A3B8),
        onTertiary = Color(0xFF0F172A),
        background = Color(0xFF0F172A),
        onBackground = Color(0xFFE2E8F0),
        surface = Color(0xFF111827),
        onSurface = Color(0xFFE2E8F0),
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF334155),
    )

    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColors else lightColors,
        content = content,
    )
}
