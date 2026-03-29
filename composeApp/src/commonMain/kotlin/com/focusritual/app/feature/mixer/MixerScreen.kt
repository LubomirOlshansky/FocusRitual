package com.focusritual.app.feature.mixer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.component.PlayButton
import com.focusritual.app.core.designsystem.component.SoundTile
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.background
import org.jetbrains.compose.resources.painterResource

@Composable
fun MixerScreen(
    onStartSession: () -> Unit = {},
    viewModel: MixerViewModel = viewModel { MixerViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MixerScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onStartSession = onStartSession,
    )
}

@Composable
private fun MixerScreenContent(
    uiState: MixerUiState,
    onIntent: (MixerIntent) -> Unit,
    onStartSession: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        ImmersiveBackground()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 120.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayButton(
                        isPlaying = uiState.isPlaying,
                        onClick = { onIntent(MixerIntent.TogglePlayback) },
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = uiState.sceneSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 6.sp,
                    )
                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.35f),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                onStartSession()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "START SESSION",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        )
                    }
                }
            }
            items(
                items = uiState.sounds,
                key = { it.id },
            ) { sound ->
                SoundTile(
                    state = sound,
                    onToggle = { onIntent(MixerIntent.ToggleSound(sound.id)) },
                    onVolumeChange = { volume -> onIntent(MixerIntent.AdjustVolume(sound.id, volume)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ImmersiveBackground() {
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
                            0.0f to Color.Transparent,
                            0.35f to Color(0xFF0c0e11).copy(alpha = 0.6f),
                            0.55f to Color(0xFF0c0e11).copy(alpha = 0.92f),
                            1.0f to Color(0xFF0c0e11),
                        ),
                    ),
                ),
        )
    }
}
