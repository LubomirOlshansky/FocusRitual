package com.focusritual.app.feature.timer.ui

import com.focusritual.app.feature.timer.SessionPhase

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.background
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun TimerBackground(
    phase: SessionPhase,
    isPaused: Boolean,
    darkenOverride: Float? = null,
) {
    val baseOverlayAlpha by animateFloatAsState(
        targetValue = when {
            phase == SessionPhase.Break -> 0.92f
            isPaused -> 0.90f
            else -> 0.82f
        },
        animationSpec = tween(1500),
    )

    // darkenOverride 0→1 pushes overlay toward fully opaque
    val overlayAlpha = if (darkenOverride != null) {
        baseOverlayAlpha + (1f - baseOverlayAlpha) * darkenOverride
    } else {
        baseOverlayAlpha
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF0c0e11).copy(alpha = overlayAlpha * 0.7f),
                            0.3f to Color(0xFF0c0e11).copy(alpha = overlayAlpha),
                            1.0f to Color(0xFF0c0e11),
                        ),
                    ),
                ),
        )
    }
}

@Composable
internal fun AmbientBackgroundPulse(
    phase: SessionPhase,
    isPaused: Boolean,
) {
    val infiniteTransition = rememberInfiniteTransition()

    val pulseDuration = when {
        isPaused -> 14000
        phase == SessionPhase.Focus -> 7000
        else -> 11000
    }

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDuration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val targetAlpha by animateFloatAsState(
        targetValue = when {
            isPaused -> 0.015f
            phase == SessionPhase.Focus -> 0.06f
            else -> 0.025f
        },
        animationSpec = tween(1500),
    )

    val midnightBlue = Color(0xFF1B2838)
    val blueGrey = Color(0xFF2A3A4E)
    val scale = 0.9f + pulse * 0.2f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = targetAlpha
            }
            .background(
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to blueGrey.copy(alpha = 0.4f),
                        0.3f to midnightBlue.copy(alpha = 0.2f),
                        0.6f to Color.Transparent,
                        1.0f to Color.Transparent,
                    ),
                    radius = 700f,
                ),
            ),
    )
}
