package com.focusritual.app.feature.mixer.ui

import com.focusritual.app.core.designsystem.component.VolumeSlider
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusritual.app.feature.mixer.domain.SoundState

@Composable
fun SoundTile(
    state: SoundState,
    onToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onToggleOrganicMotion: () -> Unit = {},
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (state.isEnabled) {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.75f)
        },
        animationSpec = tween(300),
    )

    val borderColor by animateColorAsState(
        targetValue = if (state.isEnabled) {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.09f)
        },
        animationSpec = tween(300),
    )

    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(backgroundColor)
            .border(0.5.dp, borderColor, cardShape)
            .padding(start = 14.dp, end = 14.dp, top = 13.dp, bottom = 12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon container
                val iconBg by animateColorAsState(
                    targetValue = if (state.isEnabled) {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
                    },
                    animationSpec = tween(300),
                )
                val iconBorder by animateColorAsState(
                    targetValue = if (state.isEnabled) {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
                    },
                    animationSpec = tween(300),
                )
                val iconTint by animateColorAsState(
                    targetValue = if (state.isEnabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
                    },
                    animationSpec = tween(300),
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg)
                        .border(0.5.dp, iconBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = state.icon,
                        contentDescription = state.name,
                        modifier = Modifier.size(17.dp),
                        tint = iconTint,
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Sound name
                val nameColor by animateColorAsState(
                    targetValue = if (state.isEnabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    },
                    animationSpec = tween(300),
                )
                Text(
                    text = state.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light,
                    color = nameColor,
                )

                Spacer(Modifier.weight(1f))

                // Organic motion icon — fade in/out
                val organicAlpha by animateFloatAsState(
                    targetValue = when {
                        state.organicMotion && state.isEnabled -> 0.80f
                        state.isEnabled -> 0.28f
                        else -> 0f
                    },
                    animationSpec = tween(300),
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = state.isEnabled,
                        ) { onToggleOrganicMotion() }
                        .graphicsLayer { alpha = organicAlpha },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Organic Motion",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.width(6.dp))

                // Toggle switch
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f),
                    ),
                    modifier = Modifier.scale(0.85f),
                )
            }

            Spacer(Modifier.size(8.dp))

            // Volume slider — keep existing VolumeSlider with organic motion animation
            VolumeSlider(
                value = state.volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isEnabled,
                liveValue = if (state.isEnabled) state.liveVolume else null,
            )
        }
    }
}
