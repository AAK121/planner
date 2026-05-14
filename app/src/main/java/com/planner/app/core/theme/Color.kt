package com.planner.app.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Neutral palette (shared across all themes) ───────────────────────────────
val Neutral900 = Color(0xFF1A1A18)
val Neutral800 = Color(0xFF2D2B28)
val Neutral600 = Color(0xFF5A5950)
val Neutral400 = Color(0xFF9C9A92)
val Neutral300 = Color(0xFFB8B6AE)
val Neutral200 = Color(0xFFD4D3CE)
val Neutral150 = Color(0xFFE8E7E3)
val Neutral100 = Color(0xFFF0EFEC)
val Neutral50  = Color(0xFFF8F7F5)
val NeutralSurface = Color(0xFFFAFAF8)
val NeutralBg   = Color(0xFFFFFFFF)
val NeutralOuter = Color(0xFFF4F3F0)

// ── Accent ───────────────────────────────────────────────────────────────────
val StreakOrange = Color(0xFFE8A44A)
val PositiveGreen = Color(0xFF7AC49A)

// ── Dark theme surfaces ───────────────────────────────────────────────────────
val Dark900 = Color(0xFF0A0A0A)
val Dark800 = Color(0xFF141413)
val Dark700 = Color(0xFF1E1E1C)
val Dark600 = Color(0xFF2A2A26)
val Dark300 = Color(0xFF3A3A36)

// ── Colorful theme accents ────────────────────────────────────────────────────
val ColorfulBg     = Color(0xFFF0ECE4)
val ColorfulSurface = Color(0xFFFAF8F4)
val ColorfulTeal   = Color(0xFF5B9E9E)
val ColorfulCoral  = Color(0xFFD4816B)
val ColorfulAmber  = Color(0xFFC9A24E)
val ColorfulSage   = Color(0xFF7A9E7A)
val ColorfulOrange = Color(0xFFC47A20)

// ── Heatmap intensity levels ──────────────────────────────────────────────────
val HeatmapLight0 = Color(0xFFF0EFEC)
val HeatmapLight1 = Color(0xFFD6D5D0)
val HeatmapLight2 = Color(0xFFA5A49E)
val HeatmapLight3 = Color(0xFF5A5950)
val HeatmapLight4 = Color(0xFF1A1A18)

val HeatmapDark0 = Color(0xFF1E1E1C)
val HeatmapDark1 = Color(0xFF2A2A26)
val HeatmapDark2 = Color(0xFF5A5950)
val HeatmapDark3 = Color(0xFF9C9A92)
val HeatmapDark4 = Color(0xFFE8E7E3)

// ── Material3 color schemes ───────────────────────────────────────────────────
val LightColorScheme = lightColorScheme(
    primary          = Neutral900,
    onPrimary        = NeutralBg,
    primaryContainer = NeutralOuter,
    onPrimaryContainer = Neutral900,
    secondary        = Neutral600,
    onSecondary      = NeutralBg,
    secondaryContainer = Neutral100,
    onSecondaryContainer = Neutral900,
    tertiary         = StreakOrange,
    background       = NeutralBg,
    onBackground     = Neutral900,
    surface          = NeutralSurface,
    onSurface        = Neutral900,
    surfaceVariant   = NeutralOuter,
    onSurfaceVariant = Neutral600,
    outline          = Neutral200,
    outlineVariant   = Neutral150,
)

val DarkColorScheme = darkColorScheme(
    primary          = Neutral150,
    onPrimary        = Dark800,
    primaryContainer = Dark700,
    onPrimaryContainer = Neutral150,
    secondary        = Neutral400,
    onSecondary      = Dark800,
    secondaryContainer = Dark600,
    onSecondaryContainer = Neutral150,
    tertiary         = StreakOrange,
    background       = Dark800,
    onBackground     = Neutral150,
    surface          = Dark700,
    onSurface        = Neutral150,
    surfaceVariant   = Dark600,
    onSurfaceVariant = Neutral400,
    outline          = Dark300,
    outlineVariant   = Dark600,
)

val ColorfulColorScheme = lightColorScheme(
    primary          = ColorfulTeal,
    onPrimary        = NeutralBg,
    primaryContainer = ColorfulBg,
    onPrimaryContainer = Neutral800,
    secondary        = ColorfulCoral,
    onSecondary      = NeutralBg,
    secondaryContainer = Color(0xFFFFF3E0),
    onSecondaryContainer = Neutral800,
    tertiary         = ColorfulAmber,
    background       = ColorfulBg,
    onBackground     = Neutral800,
    surface          = ColorfulSurface,
    onSurface        = Neutral800,
    surfaceVariant   = NeutralOuter,
    onSurfaceVariant = Neutral600,
    outline          = Neutral200,
    outlineVariant   = Neutral150,
)
