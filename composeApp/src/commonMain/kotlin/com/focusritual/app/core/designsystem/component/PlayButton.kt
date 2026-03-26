package com.focusritual.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private val GlowColor = Color(0xFFB7C8DB)
private val OrganicEasing = CubicBezierEasing(0.3f, 0.0f, 0.15f, 1.0f)

@Composable
fun PlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Breathing pulse when playing
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    // Glow visibility: fades in/out with play state
    val glowIntensity by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(800),
    )

    // Subtle scale pulse when playing
    val scaleAnim = 1f + breath * 0.04f * glowIntensity

    // 3 staggered breathing rings — each at a different speed
    val ring1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val ring3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6800, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val backgroundColor = animateColorAsState(
        targetValue = if (isPlaying) {
            MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.6f)
        },
        animationSpec = tween(durationMillis = 300),
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Breathing ring 3 (outermost, slowest)
        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    val s = 0.88f + ring3 * 0.12f
                    scaleX = s
                    scaleY = s
                    alpha = (0.08f + ring3 * 0.10f) * glowIntensity
                }
                .border(1.dp, GlowColor.copy(alpha = 0.14f), CircleShape),
        )

        // Breathing ring 2 (middle)
        Box(
            modifier = Modifier
                .size(175.dp)
                .graphicsLayer {
                    val s = 0.90f + ring2 * 0.10f
                    scaleX = s
                    scaleY = s
                    alpha = (0.12f + ring2 * 0.14f) * glowIntensity
                }
                .border(1.dp, GlowColor.copy(alpha = 0.18f), CircleShape),
        )

        // Breathing ring 1 (closest to button)
        Box(
            modifier = Modifier
                .size(135.dp)
                .graphicsLayer {
                    val s = 0.93f + ring1 * 0.07f
                    scaleX = s
                    scaleY = s
                    alpha = (0.16f + ring1 * 0.18f) * glowIntensity
                }
                .border(1.dp, GlowColor.copy(alpha = 0.22f), CircleShape),
        )

        // Outer glow aura
        Box(
            modifier = Modifier
                .size(165.dp)
                .graphicsLayer {
                    val auraScale = 1f + breath * 0.08f * glowIntensity
                    scaleX = auraScale
                    scaleY = auraScale
                    alpha = (0.30f + breath * 0.25f) * glowIntensity
                }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to GlowColor.copy(alpha = 0.20f),
                                0.3f to GlowColor.copy(alpha = 0.10f),
                                0.6f to GlowColor.copy(alpha = 0.03f),
                                1.0f to Color.Transparent,
                            ),
                            radius = size.width * 0.5f,
                        ),
                    )
                },
        )

        // Inner halo ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                    alpha = (0.20f + breath * 0.18f) * glowIntensity
                }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.4f to GlowColor.copy(alpha = 0.08f),
                                0.7f to GlowColor.copy(alpha = 0.14f),
                                1.0f to Color.Transparent,
                            ),
                            radius = size.width * 0.5f,
                        ),
                    )
                },
        )

        // The button itself
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                }
                .shadow(elevation = 24.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(backgroundColor.value)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}
