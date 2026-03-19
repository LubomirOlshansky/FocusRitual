package com.focusritual.app.feature.mixer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.component.PlayButton

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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            PlayButton(
                isPlaying = uiState.isPlaying,
                onClick = { onIntent(MixerIntent.TogglePlayback) },
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = uiState.sceneName,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.sceneSubtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 6.sp,
            )
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
