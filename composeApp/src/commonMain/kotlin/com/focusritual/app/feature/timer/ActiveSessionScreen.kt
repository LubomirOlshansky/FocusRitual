package com.focusritual.app.feature.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.feature.session.SessionConfig
import kotlinx.coroutines.delay

@Composable
fun ActiveSessionScreen(
    config: SessionConfig,
    onFinish: () -> Unit,
    onSoundControl: (Float?) -> Unit,
    viewModel: ActiveSessionViewModel = viewModel { ActiveSessionViewModel(config) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isExiting by remember { mutableStateOf(false) }

    val targetVolume = when {
        isExiting || uiState.isCompleted -> 0f
        uiState.isPaused -> 0f
        uiState.phase == SessionPhase.Focus -> 1f
        else -> 0f
    }

    val animatedVolume by animateFloatAsState(
        targetValue = targetVolume,
        animationSpec = tween(400),
    )

    LaunchedEffect(animatedVolume) {
        onSoundControl(animatedVolume)
    }

    LaunchedEffect(isExiting) {
        if (isExiting) {
            delay(450L)
            onFinish()
        }
    }

    if (uiState.isCompleted) {
        LaunchedEffect(Unit) {
            delay(2000L)
            isExiting = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { onSoundControl(null) }
    }

    ActiveSessionScreenContent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                ActiveSessionIntent.Stop -> isExiting = true
                else -> viewModel.onIntent(intent)
            }
        },
    )
}

@Composable
private fun ActiveSessionScreenContent(
    uiState: ActiveSessionUiState,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        TimerBackground(phase = uiState.phase, isPaused = uiState.isPaused)
        AmbientBackgroundPulse(phase = uiState.phase, isPaused = uiState.isPaused)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            SessionTopBar(onClose = { onIntent(ActiveSessionIntent.Stop) })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.phaseLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )

                    Spacer(Modifier.height(48.dp))

                    Box(contentAlignment = Alignment.Center) {
                        AtmosphericField(
                            phase = uiState.phase,
                            isPaused = uiState.isPaused || uiState.isCompleted,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = uiState.remainingFormatted,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.ExtraLight,
                                    letterSpacing = (-2).sp,
                                    fontFamily = FontFamily.Default,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.widthIn(min = 220.dp),
                                )

                                Spacer(Modifier.height(16.dp))

                                if (!uiState.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
                                            )
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                            ) { onIntent(ActiveSessionIntent.TogglePause) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            contentDescription = if (uiState.isPaused) "Resume" else "Pause",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ProgressSection(
                currentCycle = uiState.currentCycle,
                totalCycles = uiState.totalCycles,
                isCompleted = uiState.isCompleted,
            )

            Spacer(Modifier.height(24.dp))

            if (!uiState.isCompleted) {
                BottomControls(
                    onSkip = { onIntent(ActiveSessionIntent.Skip) },
                    onStop = { onIntent(ActiveSessionIntent.Stop) },
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
