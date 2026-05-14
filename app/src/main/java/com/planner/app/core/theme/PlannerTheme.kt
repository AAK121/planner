package com.planner.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeVariant { LIGHT, DARK, COLORFUL }

val LocalThemeVariant = staticCompositionLocalOf { ThemeVariant.LIGHT }

@Composable
fun PlannerTheme(
    variant: ThemeVariant = ThemeVariant.LIGHT,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (variant) {
        ThemeVariant.LIGHT     -> LightColorScheme
        ThemeVariant.DARK      -> DarkColorScheme
        ThemeVariant.COLORFUL  -> ColorfulColorScheme
    }

    CompositionLocalProvider(LocalThemeVariant provides variant) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = PlannerTypography,
            shapes      = PlannerShapes,
            content     = content,
        )
    }
}

val ThemeVariant.isColorful get() = this == ThemeVariant.COLORFUL
val ThemeVariant.isDark     get() = this == ThemeVariant.DARK
