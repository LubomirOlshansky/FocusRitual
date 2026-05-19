package com.focusritual.app.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun PulsingTapHint(text: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        fontSize = 9.sp,
        letterSpacing = 0.22.em,
        fontWeight = FontWeight.W300,
    )
}

@Composable
fun ShimmerPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
    )
    val transition = rememberInfiniteTransition()
    val shimmerProgress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val bg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    val shimmer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Box(
        modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(0.5.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(
                indication = null,
                interactionSource = interactionSource,
            ) { onClick() }
            .drawWithContent {
                drawContent()
                val sweepWidth = size.width * 0.6f
                val travel = size.width + sweepWidth
                val originX = -sweepWidth + travel * ((shimmerProgress + 1f) / 2f)
                drawRect(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.5f to shimmer,
                            1f to Color.Transparent,
                        ),
                        start = androidx.compose.ui.geometry.Offset(originX, 0f),
                        end = androidx.compose.ui.geometry.Offset(originX + sweepWidth, size.height),
                    ),
                )
            }
            .padding(horizontal = 36.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
            fontSize = 10.sp,
            letterSpacing = 0.2.em,
            fontWeight = FontWeight.W300,
        )
    }
}
