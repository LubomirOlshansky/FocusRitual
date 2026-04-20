package com.focusritual.app.feature.mixer.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.theme.GlowColor
import com.focusritual.app.core.designsystem.theme.OrganicEasing


@Composable
internal fun HeroSessionButton(
    isPlaying: Boolean,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()

    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val glowIntensity by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(800),
    )

    val scaleAnim = 1f + breath * 0.025f * glowIntensity
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.85f else 0.65f,
        animationSpec = tween(500),
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(150),
    )

    val surfaceBright = MaterialTheme.colorScheme.surfaceBright
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val scrim = MaterialTheme.colorScheme.scrim

    Box(
        modifier = modifier
            .size(150.dp)
            .graphicsLayer {
                scaleX = scaleAnim * pressScale
                scaleY = scaleAnim * pressScale
            }
            .drawBehind {
                // Outer soft glow aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to GlowColor.copy(alpha = 0.20f),
                            0.3f to GlowColor.copy(alpha = 0.10f),
                            0.6f to GlowColor.copy(alpha = 0.03f),
                            1.0f to Color.Transparent,
                        ),
                        radius = size.width * 0.9f,
                    ),
                    alpha = glowIntensity,
                )
                // Inner luminous core — adds depth to center
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to onSurface.copy(alpha = 0.10f),
                            0.3f to onSurface.copy(alpha = 0.04f),
                            1.0f to Color.Transparent,
                        ),
                        radius = size.width * 0.35f,
                    ),
                    alpha = 0.6f + glowIntensity * 0.4f,
                )
                // Soft inner shadow ring — creates concavity/depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.75f to Color.Transparent,
                            0.92f to scrim.copy(alpha = 0.08f),
                            1.0f to scrim.copy(alpha = 0.15f),
                        ),
                        radius = size.width * 0.5f,
                    ),
                )
            }
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to surfaceBright.copy(alpha = bgAlpha),
                        0.4f to surfaceBright.copy(alpha = bgAlpha * 0.88f),
                        0.8f to surfaceBright.copy(alpha = bgAlpha * 0.7f),
                        1.0f to surfaceBright.copy(alpha = bgAlpha * 0.55f),
                    ),
                ),
            )
            .border(
                width = 0.5.dp,
                color = outlineVariant.copy(alpha = 0.14f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onStartSession() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "START SESSION",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f),
        )
    }
}
