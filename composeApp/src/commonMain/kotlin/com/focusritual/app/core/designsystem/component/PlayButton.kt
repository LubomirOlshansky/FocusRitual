package com.focusritual.app.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Master visibility for rings/glow (fades out when paused)
    val ringVisibility by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseOut),
    )

    // Infinite transition for pulsing rings and breathing glow
    val infiniteTransition = rememberInfiniteTransition()

    // 3 staggered ring progress values (0f → 1f over 7000ms, slow meditative pace)
    val ringProgresses = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 7000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(offsetMillis = index * 2333),
            ),
        )
    }

    // Breathing glow radius and alpha (visible but calm)
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 52f,
        targetValue = 62f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    // Initial pulse wave (one-shot on play start)
    val initialPulseRadius = remember { Animatable(0f) }
    val initialPulseAlpha = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            launch {
                initialPulseRadius.snapTo(48f)
                initialPulseRadius.animateTo(
                    targetValue = 180f,
                    animationSpec = tween(1200, easing = EaseOut),
                )
            }
            launch {
                initialPulseAlpha.snapTo(0.18f)
                initialPulseAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(1200, easing = EaseOut),
                )
            }
        } else {
            initialPulseRadius.snapTo(0f)
            initialPulseAlpha.snapTo(0f)
        }
    }

    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
    )

    // Button background color animation
    val backgroundColor by animateColorAsState(
        targetValue = if (isPlaying) {
            MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.6f)
        },
        animationSpec = tween(durationMillis = 300),
    )

    // Ring configuration (dark tones, visible pulse)
    val ringColors = listOf(
        Color(0xFF2a3a4a),
        Color(0xFF344860),
        Color(0xFF253548),
    )
    val ringPeakAlphas = listOf(0.18f, 0.14f, 0.10f)
    val ringMaxRadii = listOf(120f, 140f, 160f)
    val ringMinRadius = 48f

    Box(
        modifier = modifier.size(320.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Canvas for rings, glow, and initial pulse
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Breathing glow
            val effectiveGlowAlpha = glowAlpha * ringVisibility
            if (effectiveGlowAlpha > 0f) {
                val glowRadiusPx = glowRadius.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFa9bbcd).copy(alpha = effectiveGlowAlpha),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = glowRadiusPx,
                    ),
                    radius = glowRadiusPx,
                    center = center,
                )
            }

            // 3 pulsing rings — soft blurred multi-pass for diffused appearance
            for (i in 0..2) {
                val progress = ringProgresses[i].value
                val radius = (ringMinRadius + (ringMaxRadii[i] - ringMinRadius) * progress).dp.toPx()
                val baseAlpha = ringPeakAlphas[i] * (1f - progress) * ringVisibility
                if (baseAlpha > 0.001f) {
                    // Outer haze pass (wide, visible diffusion)
                    drawCircle(
                        color = ringColors[i].copy(alpha = baseAlpha * 0.35f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 14.dp.toPx()),
                    )
                    // Mid glow pass
                    drawCircle(
                        color = ringColors[i].copy(alpha = baseAlpha * 0.65f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6.dp.toPx()),
                    )
                    // Core pass
                    drawCircle(
                        color = ringColors[i].copy(alpha = baseAlpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }

            // Initial pulse wave (soft, blurred)
            if (initialPulseAlpha.value > 0.001f) {
                val pulseRadiusPx = initialPulseRadius.value.dp.toPx()
                val pulseColor = Color(0xFF5a6e82)
                // Outer haze
                drawCircle(
                    color = pulseColor.copy(alpha = initialPulseAlpha.value * 0.3f),
                    radius = pulseRadiusPx,
                    center = center,
                    style = Stroke(width = 12.dp.toPx()),
                )
                // Core
                drawCircle(
                    color = pulseColor.copy(alpha = initialPulseAlpha.value * 0.7f),
                    radius = pulseRadiusPx,
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
        }

        // Center button
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(elevation = 24.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                        .togetherWith(fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f))
                },
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
    }
}
