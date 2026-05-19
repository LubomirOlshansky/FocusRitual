package com.focusritual.app.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.focusritual.app.core.designsystem.softBlur
import kotlin.math.roundToInt
import kotlin.random.Random

private data class ParticleSeed(
    val startXFraction: Float,
    val startYFraction: Float,
    val durationMs: Int,
    val driftX: Dp,
    val driftY: Dp,
    val maxAlpha: Float,
    val phaseOffsetMs: Int,
)

@Composable
fun DriftingParticleField(count: Int, modifier: Modifier = Modifier) {
    val seeds = remember(count) {
        List(count) {
            ParticleSeed(
                startXFraction = Random.nextFloat(),
                startYFraction = Random.nextFloat(),
                durationMs = Random.nextInt(11_000, 16_000),
                driftX = Random.nextInt(-12, 13).dp,
                driftY = Random.nextInt(-16, -5).dp,
                maxAlpha = Random.nextFloat() * 0.5f + 0.3f,
                phaseOffsetMs = Random.nextInt(0, 5000),
            )
        }
    }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        seeds.forEach { seed ->
            FloatingMote(seed = seed, areaWidth = w, areaHeight = h)
        }
    }
}

@Composable
private fun FloatingMote(seed: ParticleSeed, areaWidth: Dp, areaHeight: Dp) {
    val density = LocalDensity.current
    val transition = rememberInfiniteTransition()
    val spec = infiniteRepeatable<Float>(
        animation = tween(seed.durationMs, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
        initialStartOffset = StartOffset(seed.phaseOffsetMs),
    )
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = spec,
    )
    val baseXPx = with(density) { (areaWidth * seed.startXFraction).toPx() }
    val baseYPx = with(density) { (areaHeight * seed.startYFraction).toPx() }
    val driftXPx = with(density) { seed.driftX.toPx() }
    val driftYPx = with(density) { seed.driftY.toPx() }
    val alpha = seed.maxAlpha * progress
    val motColor = MaterialTheme.colorScheme.onSurface
    Box(
        Modifier
            .offset {
                IntOffset(
                    x = (baseXPx + driftXPx * progress).roundToInt(),
                    y = (baseYPx + driftYPx * progress).roundToInt(),
                )
            }
            .size(2.dp)
            .softBlur(0.3.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(motColor, CircleShape),
    )
}
