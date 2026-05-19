package com.focusritual.app.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focusritual.app.core.designsystem.softBlur
import com.focusritual.app.core.designsystem.theme.OrganicEasing

@Composable
fun BreathingOrb(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    intensity: Float = 1f,
) {
    val transition = rememberInfiniteTransition()

    val outerScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val middleScale by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val centralScale by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val outerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f * intensity)
    val middleColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f * intensity)
    val coreBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f * intensity)
    val coreInner = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f * intensity)
    val coreMid = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f * intensity)
    val haloColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f * intensity)

    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size * 1.5f)
                .softBlur(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to haloColor,
                            1f to Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = outerScale
                    scaleY = outerScale
                }
                .border(0.5.dp, outerColor, CircleShape),
        )
        Box(
            Modifier
                .size(size * 0.78f)
                .graphicsLayer {
                    scaleX = middleScale
                    scaleY = middleScale
                }
                .border(0.5.dp, middleColor, CircleShape),
        )
        Box(
            Modifier
                .size(size * 0.5f)
                .graphicsLayer {
                    scaleX = centralScale
                    scaleY = centralScale
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to coreInner,
                            0.65f to coreMid,
                            1f to Color.Transparent,
                        ),
                    ),
                )
                .border(0.5.dp, coreBorder, CircleShape),
        )
    }
}
