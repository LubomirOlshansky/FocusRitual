package com.focusritual.app.feature.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
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
import com.focusritual.app.feature.session.SessionMode
import kotlinx.coroutines.delay

private const val SLEEP_FADE_OUT_MS = 30_000L

@Composable
fun ActiveSessionScreen(
    config: SessionConfig,
    sessionKey: Int = 0,
    onFinish: () -> Unit,
    onSoundControl: (Float?) -> Unit,
    onStartAnother: () -> Unit,
    viewModel: ActiveSessionViewModel = viewModel(key = "session_$sessionKey") {
        ActiveSessionViewModel(config)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isExiting by remember { mutableStateOf(false) }
    var isStartingAnother by remember { mutableStateOf(false) }

    LaunchedEffect(isStartingAnother) {
        if (isStartingAnother) {
            delay(450L)
            onStartAnother()
        }
    }

    // Sleep fade-out: use config duration when available, fallback to constant
    val sleepFadeOutMs = if (config.mode == SessionMode.Sleep && config.sleepFadeOutMinutes > 0) {
        (config.sleepFadeOutMinutes * 60 * 1000L).coerceAtLeast(1000L)
    } else {
        SLEEP_FADE_OUT_MS
    }

    // Sleep fade-out: animated presence that goes 1→0
    val sleepFadeTarget = if (uiState.isSleepFadingOut) 0f else 1f
    val sleepFade by animateFloatAsState(
        targetValue = sleepFadeTarget,
        animationSpec = tween(
            durationMillis = sleepFadeOutMs.toInt(),
            easing = EaseInOut,
        ),
    )

    // Volume logic
    val targetVolume = when {
        isExiting -> 0f
        isStartingAnother -> 0f
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

    DisposableEffect(Unit) {
        onDispose { onSoundControl(null) }
    }

    ActiveSessionScreenContent(
        uiState = uiState,
        sleepFade = sleepFade,
        onStartAnother = { isStartingAnother = true },
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
    onStartAnother: () -> Unit,
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

        AnimatedContent(
            targetState = uiState.isCompleted && !uiState.isSleepMode,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(tween(600, easing = FocusRitualEasing.Atmospheric)) +
                        scaleIn(tween(700, easing = FocusRitualEasing.Ritual), initialScale = 0.97f)) togetherWith
                        fadeOut(tween(400))
                } else {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                }
            },
            label = "sessionComplete",
        ) { showCompletion ->
            if (showCompletion) {
                SessionCompleteScreen(
                    onReturnToMixer = { onIntent(ActiveSessionIntent.Stop) },
                    onStartAnother = onStartAnother,
                )
            } else {
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
                        modifier = Modifier.graphicsLayer { alpha = if (uiState.isCompleted) 0f else 1f },
                    )

                    Spacer(Modifier.height(48.dp))

                    Box(contentAlignment = Alignment.Center) {
                        AtmosphericField(
                            phase = uiState.phase,
                            isPaused = uiState.isPaused || uiState.isCompleted,
                            isSleepMode = uiState.isSleepMode,
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
                                        modifier = Modifier
                                            .widthIn(min = 220.dp)
                                            .graphicsLayer { alpha = if (uiState.isCompleted) 0f else 1f },
                                    )

                                    Spacer(Modifier.height(16.dp))
                                }

                                if (!uiState.isCompleted && !isSleepFading && !uiState.isSleepMode) {
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
                if (!uiState.isSleepMode) {
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
                } else if (!uiState.isCompleted) {
                    SleepExitButton(
                        onStop = { onIntent(ActiveSessionIntent.Stop) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
            }
        }
    }
}
