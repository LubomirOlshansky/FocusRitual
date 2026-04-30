package com.focusritual.app.feature.mixer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.feature.mixer.domain.MixPreset

@Composable
internal fun PresetCard(
    preset: MixPreset,
    soundNamesById: Map<String, String>,
    isLoaded: Boolean,
    onLoad: () -> Unit,
    modifier: Modifier = Modifier,
    pulseToken: Int = 0,
) {
    val shape = RoundedCornerShape(16.dp)
    val primary = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val borderColor = if (isLoaded) {
        primary.copy(alpha = 0.30f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !isLoaded) 0.98f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FocusRitualEasing.DeepEaseOut),
        label = "presetCardScale",
    )
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(pulseToken, isLoaded) {
        pulseScale.snapTo(1f)
        if (isLoaded && pulseToken > 0) {
            pulseScale.animateTo(
                targetValue = 1.018f,
                animationSpec = tween(durationMillis = 170, easing = FocusRitualEasing.DeepEaseOut),
            )
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FocusRitualEasing.DeepEaseOut),
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val resolvedScale = pressScale * pulseScale.value
                scaleX = resolvedScale
                scaleY = resolvedScale
            }
            .clip(shape)
            .background(backgroundColor, shape)
            .let { base ->
                if (isLoaded) {
                    base.background(primary.copy(alpha = 0.07f), shape)
                } else {
                    base
                }
            }
            .border(width = 0.5.dp, color = borderColor, shape = shape)
            .let { base ->
                if (isLoaded) {
                    base
                } else {
                    base.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) { onLoad() }
                }
            }
            .padding(start = 14.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PresetThumbnail(isLoaded = isLoaded)

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = preset.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isLoaded) 0.92f else 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = presetMeta(preset, soundNamesById),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isLoaded) 0.58f else 0.48f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (isLoaded) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f)),
                )
            }
        } else {
            PresetLoadButton(onLoad = onLoad)
        }
    }
}

@Composable
private fun PresetThumbnail(isLoaded: Boolean) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isLoaded) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    }
    val borderColor = if (isLoaded) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
    }
    val tint = if (isLoaded) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(bgColor)
            .border(width = 0.5.dp, color = borderColor, shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint,
        )
    }
}

@Composable
private fun PresetLoadButton(onLoad: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FocusRitualEasing.DeepEaseOut),
        label = "presetLoadButtonScale",
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.82f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onLoad() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Load mix",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
        )
    }
}

private fun presetMeta(
    preset: MixPreset,
    soundNamesById: Map<String, String>,
): String {
    val soundNames = preset.sounds.map { sound -> soundNamesById[sound.id] ?: sound.id }
    val count = preset.sounds.size
    val countText = if (count == 1) "1 sound" else "$count sounds"
    return if (soundNames.isEmpty()) countText else soundNames.joinToString(" · ") + " · " + countText
}
