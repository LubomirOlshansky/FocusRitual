package com.focusritual.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.focusritual.app.feature.mixer.model.SoundIcon
import com.focusritual.app.feature.mixer.model.SoundState

private fun SoundIcon.toImageVector(): ImageVector = when (this) {
    SoundIcon.Rain -> Icons.Filled.WaterDrop
    SoundIcon.Thunder -> Icons.Filled.Thunderstorm
    SoundIcon.Wind -> Icons.Filled.Air
    SoundIcon.Forest -> Icons.Filled.Forest
    SoundIcon.Stream -> Icons.Filled.Water
    SoundIcon.Cafe -> Icons.Filled.LocalCafe
    SoundIcon.Fireplace -> Icons.Filled.Fireplace
    SoundIcon.BrownNoise -> Icons.Filled.GraphicEq
    SoundIcon.Waves -> Icons.Filled.Water
}

@Composable
fun SoundTile(
    state: SoundState,
    onToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (state.isEnabled) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
        },
        animationSpec = tween(300),
    )

    val borderAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (state.isEnabled) 0.35f else 0.15f,
        animationSpec = tween(300),
    )
    val border = BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = borderAlpha),
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = state.icon.toImageVector(),
                    contentDescription = state.name,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = state.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onToggle,
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
            Spacer(Modifier.size(8.dp))
            VolumeSlider(
                value = state.volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isEnabled,
            )
        }
    }
}
