package com.focusritual.app.feature.mixer.ui.modal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.component.VolumeSlider
import com.focusritual.app.feature.mixer.domain.SoundState
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.mixer_organic_motion
import focusritual.composeapp.generated.resources.remove
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ActiveSoundRow(
    sound: SoundState,
    onAdjustVolume: (Float) -> Unit,
    onToggleOrganicMotion: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val sparkleAlpha by animateFloatAsState(
        targetValue = if (sound.organicMotion) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "activeSoundSparkleAlpha",
    )
    val sparkleBackgroundColor = lerp(
        start = colorScheme.surfaceContainerHighest.copy(alpha = 0.90f),
        stop = colorScheme.primary.copy(alpha = 0.10f),
        fraction = sparkleAlpha,
    )
    val sparkleBorderColor = lerp(
        start = colorScheme.outlineVariant.copy(alpha = 0.38f),
        stop = colorScheme.primary.copy(alpha = 0.22f),
        fraction = sparkleAlpha,
    )
    val sparkleIconTint = lerp(
        start = colorScheme.onSurface.copy(alpha = 0.55f),
        stop = colorScheme.primary.copy(alpha = 0.82f),
        fraction = sparkleAlpha,
    )

    LuxuryCardSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = 13.dp, end = 13.dp, top = 13.dp, bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.onSurface.copy(alpha = 0.04f))
                        .border(
                            width = 0.5.dp,
                            color = colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = sound.icon,
                        contentDescription = sound.name,
                        modifier = Modifier.size(12.dp),
                        tint = colorScheme.onSurface.copy(alpha = 0.48f),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = sound.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.01f).em,
                    color = colorScheme.onSurface.copy(alpha = 0.84f),
                )
                Spacer(Modifier.weight(1f))

                CircleIconButton(
                    onClick = onToggleOrganicMotion,
                    backgroundColor = sparkleBackgroundColor,
                    borderColor = sparkleBorderColor,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = stringResource(Res.string.mixer_organic_motion),
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer { alpha = 0.55f + (0.45f * sparkleAlpha) },
                        tint = sparkleIconTint,
                    )
                }
                Spacer(Modifier.width(6.dp))
                CircleIconButton(
                    onClick = onRemove,
                    backgroundColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.80f),
                    borderColor = colorScheme.outlineVariant.copy(alpha = 0.32f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.remove),
                        modifier = Modifier.size(11.dp),
                        tint = colorScheme.onSurface.copy(alpha = 0.50f),
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
