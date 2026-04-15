package com.focusritual.app.feature.mixer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.component.VolumeSlider
import com.focusritual.app.core.designsystem.component.toImageVector
import com.focusritual.app.feature.mixer.model.SoundState

@Composable
fun CurrentMixModal(
    isVisible: Boolean,
    uiState: MixerUiState,
    onIntent: (MixerIntent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeSounds = uiState.sounds.filter { it.isEnabled }

    LaunchedEffect(activeSounds.isEmpty()) {
        if (activeSounds.isEmpty()) onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible && activeSounds.isNotEmpty(),
        enter = fadeIn(tween(350)) + slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it },
        exit = fadeOut(tween(250)) + slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0c0e11).copy(alpha = 0.98f))
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ModalHeader(
                    activeSoundCount = activeSounds.size,
                    isPlaying = uiState.isPlaying,
                    onDismiss = onDismiss,
                    onTogglePlayback = { onIntent(MixerIntent.TogglePlayback) },
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                ) {
                    item {
                        GlobalOrganicMotionRow(
                            checked = uiState.sounds.any { it.isEnabled && it.organicMotion },
                            onToggle = { onIntent(MixerIntent.ToggleGlobalOrganicMotion) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(
                        items = activeSounds,
                        key = { it.id },
                    ) { sound ->
                        ActiveSoundRow(
                            sound = sound,
                            onAdjustVolume = { v -> onIntent(MixerIntent.AdjustVolume(sound.id, v)) },
                            onToggleOrganicMotion = { onIntent(MixerIntent.ToggleOrganicMotion(sound.id)) },
                            onRemove = { onIntent(MixerIntent.RemoveFromMix(sound.id)) },
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 24.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            DoneButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun ModalHeader(
    activeSoundCount: Int,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CURRENT MIX",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Text(
                text = "$activeSoundCount sounds active",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                .clickable { onTogglePlayback() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun GlobalOrganicMotionRow(
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Organic Motion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "For all active sounds",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceBright,
                checkedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
        )
    }
}

@Composable
private fun ActiveSoundRow(
    sound: SoundState,
    onAdjustVolume: (Float) -> Unit,
    onToggleOrganicMotion: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderAlpha by animateFloatAsState(
        targetValue = 0.25f,
        animationSpec = tween(300),
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = borderAlpha),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = sound.icon.toImageVector(),
                    contentDescription = sound.name,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))

                val motionTint by animateColorAsState(
                    targetValue = if (sound.organicMotion) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
                    },
                    animationSpec = tween(300),
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onToggleOrganicMotion() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Organic Motion",
                        modifier = Modifier.size(16.dp),
                        tint = motionTint,
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
            }
            VolumeSlider(
                value = sound.volume,
                onValueChange = onAdjustVolume,
                modifier = Modifier.fillMaxWidth(),
                enabled = true,
                liveValue = sound.liveVolume,
            )
        }
    }
}

@Composable
private fun DoneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.50f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Done",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        )
    }
}
