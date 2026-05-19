package com.focusritual.app.feature.onboarding.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush

@Composable
internal fun FilmGrainOverlay(modifier: Modifier = Modifier) {
    val grain = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f)
    Canvas(modifier.fillMaxSize()) {
        val stride = 3f
        val brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0f to grain,
                (1f / stride) to androidx.compose.ui.graphics.Color.Transparent,
            ),
            startY = 0f,
            endY = stride,
            tileMode = androidx.compose.ui.graphics.TileMode.Repeated,
        )
        drawRect(
            brush = brush,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            blendMode = BlendMode.Overlay,
        )
    }
}
