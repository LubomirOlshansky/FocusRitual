package com.focusritual.app.feature.mixer

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.component.PlayButton
import com.focusritual.app.core.designsystem.component.SoundTile

@Composable
fun MixerScreen(viewModel: MixerViewModel = viewModel { MixerViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MixerScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun MixerScreenContent(
    uiState: MixerUiState,
    onIntent: (MixerIntent) -> Unit,
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
                        .padding(top = 48.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayButton(
                        isPlaying = uiState.isPlaying,
                        onClick = { onIntent(MixerIntent.TogglePlayback) },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiState.sceneName,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.sceneSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 6.sp,
                        textAlign = TextAlign.Center,
                    )
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
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF0a1a12),
                        0.4f to Color(0xFF0c0e11),
                        1.0f to Color(0xFF0c0e11),
                    ),
                ),
            ),
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
    )
}
