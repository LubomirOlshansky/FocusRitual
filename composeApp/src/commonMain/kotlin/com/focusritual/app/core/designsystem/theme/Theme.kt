package com.focusritual.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun FocusRitualTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = Primary,
        onPrimary = Surface,
        primaryContainer = PrimaryContainer,
        onPrimaryContainer = Primary,
        surface = Surface,
        onSurface = OnSurface,
        onSurfaceVariant = OnSurfaceVariant,
        surfaceContainerLowest = SurfaceContainerLowest,
        surfaceContainerLow = SurfaceContainerLow,
        surfaceContainer = SurfaceContainer,
        surfaceContainerHigh = SurfaceContainerHigh,
        surfaceContainerHighest = SurfaceContainerHighest,
        surfaceBright = SurfaceBright,
        outlineVariant = OutlineVariant,
        outline = Outline,
        tertiary = Tertiary,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FocusRitualTypography,
        content = content,
    )
}
