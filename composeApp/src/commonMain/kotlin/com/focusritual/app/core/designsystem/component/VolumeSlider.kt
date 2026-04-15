package com.focusritual.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focusritual.app.core.designsystem.theme.PrimaryDim
import com.focusritual.app.core.designsystem.theme.SecondaryFixedDim

// Soft accent shift when organic motion is active
private val OrganicAccent = Color(0xFF8EC5E2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    liveValue: Float? = null,
) {
    val trackShape = RoundedCornerShape(4.dp)
    val trackHeight = 4.dp
    val hasLive = liveValue != null && enabled

    val animatedLive by animateFloatAsState(
        targetValue = liveValue ?: value,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 200f),
    )

    // Smooth color transition when organic motion toggles on/off
    val trackStartColor by animateColorAsState(
        targetValue = if (!enabled) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
        } else if (hasLive) {
            OrganicAccent
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(500),
    )
    val trackEndColor by animateColorAsState(
        targetValue = if (!enabled) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        } else if (hasLive) {
            PrimaryDim.copy(alpha = 0.85f)
        } else {
            PrimaryDim
        },
        animationSpec = tween(500),
    )

    val thumbColor by animateColorAsState(
        targetValue = if (enabled) {
            SecondaryFixedDim
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
        },
        animationSpec = tween(300),
    )

    val inactiveTrackAlpha = if (enabled) 1f else 0.80f

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        thumb = {
            Box(
                Modifier
                    .size(14.dp)
                    .shadow(2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(thumbColor),
            )
        },
        track = { sliderState ->
            val baseFraction = (sliderState.value - sliderState.valueRange.start) /
                (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

            val displayFraction = if (hasLive) {
                animatedLive.coerceIn(0f, 1f)
            } else {
                baseFraction.coerceIn(0f, 1f)
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(trackShape)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = inactiveTrackAlpha),
                    ),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(displayFraction)
                        .height(trackHeight)
                        .clip(trackShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(trackStartColor, trackEndColor),
                            ),
                        ),
                )
            }
        },
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent,
        ),
    )
}
