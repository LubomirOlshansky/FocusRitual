package com.focusritual.app.feature.onboarding.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
internal fun DriftingMistLayer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val drift by transition.animateFloat(
        initialValue = -12f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val density = LocalDensity.current
    val mistColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    Canvas(
        modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = with(density) { drift.dp.toPx() }
            },
    ) {
        val maxDim = maxOf(size.width, size.height)
        val radius1 = maxDim * 0.55f
        val center1 = Offset(size.width * 0.30f, size.height * 0.60f)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(mistColor, Color.Transparent),
                center = center1,
                radius = radius1,
            ),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            blendMode = BlendMode.Screen,
        )
        val radius2 = maxDim * 0.55f
        val center2 = Offset(size.width * 0.75f, size.height * 0.40f)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(mistColor, Color.Transparent),
                center = center2,
                radius = radius2,
            ),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            blendMode = BlendMode.Screen,
        )
    }
}
