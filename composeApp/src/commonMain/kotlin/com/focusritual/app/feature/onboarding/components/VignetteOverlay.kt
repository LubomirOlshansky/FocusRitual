package com.focusritual.app.feature.onboarding.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
internal fun VignetteOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val maxDim = maxOf(size.width, size.height)
        drawRect(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.3f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = 0.65f),
                ),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = maxDim * 0.75f,
            ),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
        )
    }
}
