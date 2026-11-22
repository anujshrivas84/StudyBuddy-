package com.anix.studybuddy.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Anix Neon Dark Palette ───────────────────────────────────────────────────
val NeonGreen   = Color(0xFF00FF9C)
val NeonGreenDim = Color(0xFF00CC7A)
val Black       = Color(0xFF0B0B0B)
val Surface1    = Color(0xFF111111)
val Surface2    = Color(0xFF1A1A1A)
val Surface3    = Color(0xFF222222)
val OnSurface   = Color(0xFFE0E0E0)
val OnSurfaceDim = Color(0xFF888888)
val ErrorRed    = Color(0xFFFF4444)
val WarnAmber   = Color(0xFFFFAA00)

private val DarkColorScheme = darkColorScheme(
    primary          = NeonGreen,
    onPrimary        = Black,
    primaryContainer = Surface3,
    onPrimaryContainer = NeonGreen,
    secondary        = NeonGreenDim,
    onSecondary      = Black,
    background       = Black,
    onBackground     = OnSurface,
    surface          = Surface1,
    onSurface        = OnSurface,
    surfaceVariant   = Surface2,
    onSurfaceVariant = OnSurfaceDim,
    error            = ErrorRed,
    outline          = Surface3
)

@Composable
fun AnixTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}
