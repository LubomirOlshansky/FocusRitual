package com.focusritual.app.feature.onboarding.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focusritual.app.core.designsystem.softBlur
import com.focusritual.app.core.designsystem.theme.OrganicEasing

@Composable
fun DistantLight(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition()
    val scale by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val intensity by infinite.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Box(
            Modifier
                .size(96.dp)
                .scale(scale)
                .softBlur(24.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.14f * intensity),
                            primary.copy(alpha = 0.05f * intensity),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            Modifier
                .size(6.dp)
                .scale(scale)
                .background(
                    color = onSurface.copy(alpha = 0.55f * intensity),
                    shape = CircleShape,
                ),
        )
    }
}
