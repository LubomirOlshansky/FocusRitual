package com.focusritual.app.feature.timer

import androidx.compose.animation.core.EaseInOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.feature.session.SessionConfig
import kotlinx.coroutines.delay

private const val SLEEP_FADE_OUT_MS = 30_000L

@Composable
fun ActiveSessionScreen(
    config: SessionConfig,
    onFinish: () -> Unit,
    onSoundControl: (Float?) -> Unit,
    viewModel: ActiveSessionViewModel = viewModel { ActiveSessionViewModel(config) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isExiting by remember { mutableStateOf(false) }

    // Sleep fade-out: animated presence that goes 1→0 over SLEEP_FADE_OUT_MS
    val sleepFadeTarget = if (uiState.isSleepFadingOut) 0f else 1f
    val sleepFade by animateFloatAsState(
        targetValue = sleepFadeTarget,
        animationSpec = tween(
            durationMillis = SLEEP_FADE_OUT_MS.toInt(),
            easing = EaseInOut,
        ),
    )

    // Volume logic
    val targetVolume = when {
        isExiting -> 0f
        uiState.isSleepFadingOut -> sleepFade  // fade audio with visual
        uiState.isCompleted -> 0f
        uiState.isPaused -> 0f
        uiState.phase == SessionPhase.Focus -> 1f
        else -> 0f
    }

    val animatedVolume by animateFloatAsState(
        targetValue = targetVolume,
        animationSpec = tween(if (uiState.isSleepFadingOut) 500 else 400),
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

    // Sleep mode: auto-exit after fade-out completes
    if (uiState.isSleepFadingOut && sleepFade == 0f) {
        LaunchedEffect(Unit) {
            delay(500L)
            onFinish()
        }
    }

    // Focus mode: existing auto-exit on completion
    if (uiState.isCompleted && !uiState.isSleepMode) {
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
        sleepFade = sleepFade,
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
    sleepFade: Float = 1f,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    val isSleepFading = uiState.isSleepFadingOut

    Box(Modifier.fillMaxSize()) {
        TimerBackground(
            phase = uiState.phase,
            isPaused = uiState.isPaused,
            darkenOverride = if (isSleepFading) 1f - sleepFade else null,
        )
        AmbientBackgroundPulse(
            phase = uiState.phase,
            isPaused = uiState.isPaused || isSleepFading,
        )

        // Sleep fade-to-black overlay
        if (isSleepFading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (1f - sleepFade) * 0.6f)),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            // Hide top bar during sleep fade-out
            if (!isSleepFading) {
                SessionTopBar(onClose = { onIntent(ActiveSessionIntent.Stop) })
            } else {
                Spacer(Modifier.height(52.dp))
            }

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isSleepFading) 0.3f * sleepFade else 0.6f,
                        ),
                    )

                    Spacer(Modifier.height(48.dp))

                    Box(contentAlignment = Alignment.Center) {
                        AtmosphericField(
                            phase = uiState.phase,
                            isPaused = uiState.isPaused || uiState.isCompleted,
                            fadeFraction = if (isSleepFading) sleepFade else 1f,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = if (isSleepFading) {
                                    Modifier.graphicsLayer { alpha = sleepFade }
                                } else {
                                    Modifier
                                },
                            ) {
                                // Hide timer in sleep fade-out
                                if (!isSleepFading) {
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
                                }

                                if (!uiState.isCompleted && !isSleepFading) {
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

            if (!isSleepFading) {
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
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
