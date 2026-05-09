package com.focusritual.app.feature.mixer.ui.modal

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.component.CloseButton
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.current_mix_label
import focusritual.composeapp.generated.resources.current_mix_sounds_active
import focusritual.composeapp.generated.resources.pause
import focusritual.composeapp.generated.resources.play
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ModalHeader(
    activeSoundCount: Int,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CloseButton(onClick = onDismiss)

        Spacer(Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.current_mix_label).uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.14.em,
                color = colorScheme.onSurface.copy(alpha = 0.42f),
            )
            Text(
                text = stringResource(Res.string.current_mix_sounds_active, activeSoundCount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }

        Spacer(Modifier.weight(1f))

        CircleIconButton(
            onClick = onTogglePlayback,
            size = 28.dp,
            backgroundColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.90f),
            borderColor = colorScheme.outlineVariant.copy(alpha = 0.20f),
        ) {
            Crossfade(
                targetState = isPlaying,
                animationSpec = tween(durationMillis = 250),
                label = "modalHeaderPlaybackIcon",
            ) { isCurrentlyPlaying ->
                Icon(
                    imageVector = if (isCurrentlyPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isCurrentlyPlaying) {
                        stringResource(Res.string.pause)
                    } else {
                        stringResource(Res.string.play)
                    },
                    tint = colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
