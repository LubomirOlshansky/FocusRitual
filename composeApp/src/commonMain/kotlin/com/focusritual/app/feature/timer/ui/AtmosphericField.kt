package com.focusritual.app.feature.timer.ui

import com.focusritual.app.feature.timer.SessionPhase

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.focusritual.app.core.designsystem.theme.OrganicEasing

private val SurfaceContainer = Color(0xFF161A1F)
private val OutlineVariant = Color(0xFF424851)
private val Primary = Color(0xFFB7C8DB)

private val DriftEasing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)

@Composable
internal fun AtmosphericField(
    phase: SessionPhase,
    isPaused: Boolean,
    isSleepMode: Boolean = false,
    fadeFraction: Float = 1f,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition()

    // ── Primary breath: main rhythm ──
    val primaryHalfCycle = when {
        isSleepMode -> 6500
        isPaused -> 6000
        phase == SessionPhase.Focus -> 4800
        else -> 5800
    }

    val primaryBreath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(primaryHalfCycle, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    // ── Secondary breath: slower cycle, organic variation ──
    val secondaryHalfCycle = when {
        isSleepMode -> 10000
        isPaused -> 8500
        phase == SessionPhase.Focus -> 7200
        else -> 9000
    }

    val secondaryBreath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(secondaryHalfCycle, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    // Combined breath: primary drives, secondary adds organic drift
    val breath = primaryBreath * 0.7f + secondaryBreath * 0.3f

    // ── Phase intensity (smooth 2s transition) ──
    val intensity by animateFloatAsState(
        targetValue = when {
            isSleepMode -> 0.45f
            isPaused -> 0.3f
            phase == SessionPhase.Focus -> 1.0f
            else -> 0.5f
        },
        animationSpec = tween(2000),
    )

    val b = breath * intensity

    // ── Inner light drift: slow xy wander ──
    val driftX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isSleepMode -> 20000
                    isPaused -> 18000
                    phase == SessionPhase.Focus -> 11000
                    else -> 15000
                },
                easing = DriftEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val driftY by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isSleepMode -> 25000
                    isPaused -> 22000
                    phase == SessionPhase.Focus -> 14000
                    else -> 19000
                },
                easing = DriftEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    // Drift visibility scales with phase (separate from breath intensity)
    val driftIntensity by animateFloatAsState(
        targetValue = when {
            isSleepMode -> 0.25f
            isPaused -> 0.15f
            phase == SessionPhase.Focus -> 1.0f
            else -> 0.4f
        },
        animationSpec = tween(2500),
    )

    // ── Derived values ──
    val scaleMultiplier = if (isSleepMode) 0.6f else 1f
    val outerScale = 1f + b * 0.1f * scaleMultiplier * fadeFraction
    val outerAlpha = (0.5f + b * 0.5f) * intensity * fadeFraction
    val circleScale = 1f + b * 0.05f * scaleMultiplier * fadeFraction
    val glowAlpha = (0.15f + b * 0.10f) * intensity * fadeFraction * (if (isSleepMode) 0.6f else 1f)
    val shadowAlpha = (0.06f + (1f - b) * 0.10f) * intensity * fadeFraction

    Box(contentAlignment = Alignment.Center) {
        // Layer 1: Outer breathing pulse — large diffuse glow
        Box(
            modifier = Modifier
                .size(480.dp)
                .graphicsLayer {
                    scaleX = outerScale
                    scaleY = outerScale
                    alpha = outerAlpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Primary.copy(alpha = 0.25f),
                            0.3f to Primary.copy(alpha = 0.12f),
                            0.6f to Primary.copy(alpha = 0.03f),
                            1.0f to Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        // Layer 2: Counter-shadow ring — darkens when breath contracts
        Box(
            modifier = Modifier
                .size(370.dp)
                .graphicsLayer {
                    scaleX = circleScale
                    scaleY = circleScale
                    alpha = shadowAlpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            0.75f to Color.Black.copy(alpha = 0.22f),
                            0.9f to Color.Black.copy(alpha = 0.10f),
                            1.0f to Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        // Layer 3: Glow halo — luminance ring
        Box(
            modifier = Modifier
                .size(360.dp)
                .graphicsLayer {
                    scaleX = circleScale
                    scaleY = circleScale
                    alpha = glowAlpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Primary.copy(alpha = 0.18f),
                            0.7f to Primary.copy(alpha = 0.06f),
                            1.0f to Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        // Layer 4: Main glassmorphic circle with inner light drift
        Box(
            modifier = Modifier
                .size(320.dp)
                .graphicsLayer {
                    scaleX = circleScale
                    scaleY = circleScale
                }
                .clip(CircleShape)
                .background(SurfaceContainer.copy(alpha = 0.35f))
                .drawBehind {
                    // Inner counter-shadow: center darkens when breath contracts
                    val innerShadow = (1f - b) * 0.12f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = innerShadow),
                                0.4f to Color.Black.copy(alpha = innerShadow * 0.3f),
                                1.0f to Color.Transparent,
                            ),
                            center = center,
                            radius = size.width * 0.5f,
                        ),
                    )

                    // Inner light drift: soft light mass wandering through mist
                    val offsetX = driftX * driftIntensity * size.width * 0.12f
                    val offsetY = driftY * driftIntensity * size.height * 0.10f
                    val lightAlpha = (0.10f + b * 0.08f) * driftIntensity
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Primary.copy(alpha = lightAlpha),
                                0.4f to Primary.copy(alpha = lightAlpha * 0.4f),
                                1.0f to Color.Transparent,
                            ),
                            center = Offset(
                                x = center.x + offsetX,
                                y = center.y + offsetY,
                            ),
                            radius = size.width * 0.40f,
                        ),
                    )
                }
                .border(1.dp, OutlineVariant.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
