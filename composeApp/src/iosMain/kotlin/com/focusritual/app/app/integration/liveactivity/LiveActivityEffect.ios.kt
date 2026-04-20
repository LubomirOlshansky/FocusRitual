package com.focusritual.app.app.integration.liveactivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.focusritual.app.core.liveactivity.LiveActivityBridge
import com.focusritual.app.core.liveactivity.LiveActivityController
import com.focusritual.app.core.liveactivity.LiveActivityState
import com.focusritual.app.feature.mixer.MixerUiState
import com.focusritual.app.feature.session.SessionMode
import com.focusritual.app.feature.timer.ActiveSessionUiState
import com.focusritual.app.feature.timer.SessionPhase

@Composable
actual fun LiveActivityEffect(
    mixerState: MixerUiState,
    sessionState: ActiveSessionUiState?,
    isSessionActive: Boolean,
    onTogglePause: () -> Unit,
    onStopMix: () -> Unit,
    onSkipPhase: () -> Unit,
    onEndSession: () -> Unit,
) {
    val controller = remember { LiveActivityController() }

    // Stable references to latest callbacks — avoids restarting the DisposableEffect
    val currentTogglePause by rememberUpdatedState(onTogglePause)
    val currentStopMix by rememberUpdatedState(onStopMix)
    val currentSkipPhase by rememberUpdatedState(onSkipPhase)
    val currentEndSession by rememberUpdatedState(onEndSession)

    // Register action handler for Live Activity button taps
    DisposableEffect(Unit) {
        LiveActivityBridge.onAction = { action ->
            when (action) {
                "togglePause" -> currentTogglePause()
                "stopMix" -> currentStopMix()
                "skipPhase" -> currentSkipPhase()
                "endSession" -> currentEndSession()
            }
        }
        onDispose {
            LiveActivityBridge.onAction = null
        }
    }

    LaunchedEffect(isSessionActive, sessionState, mixerState.isPlaying, mixerState.activeSoundsSummary, mixerState.activeSoundCount) {
        when {
            // Focus session active
            isSessionActive && sessionState != null && !sessionState.isSleepMode -> {
                controller.push(
                    LiveActivityState.FocusActive(
                        remainingSeconds = sessionState.remainingSeconds,
                        totalSeconds = sessionState.totalSeconds,
                        phase = when (sessionState.phase) {
                            SessionPhase.Focus -> "Focus"
                            SessionPhase.Break -> "Break"
                        },
                        currentCycle = sessionState.currentCycle,
                        totalCycles = sessionState.totalCycles,
                        mixSummary = mixerState.activeSoundsSummary,
                        isPaused = sessionState.isPaused,
                    ),
                )
            }

            // Sleep session active
            isSessionActive && sessionState != null && sessionState.isSleepMode -> {
                controller.push(
                    LiveActivityState.SleepActive(
                        remainingSeconds = sessionState.remainingSeconds,
                        totalSeconds = sessionState.totalSeconds,
                        fadeOutMinutes = 10, // from session config — could be passed through
                        mixSummary = mixerState.activeSoundsSummary,
                        isPaused = sessionState.isPaused,
                    ),
                )
            }

            // Ambient playback (no session, mix has active sounds)
            !isSessionActive && mixerState.activeSoundCount > 0 -> {
                controller.push(
                    LiveActivityState.AmbientPlayback(
                        mixSummary = mixerState.activeSoundsSummary,
                        activeSoundCount = mixerState.activeSoundCount,
                        isPaused = !mixerState.isPlaying,
                    ),
                )
            }

            // Nothing active — stop the Live Activity
            else -> {
                controller.stop()
            }
        }
    }

    // Clean up controller when leaving composition
    DisposableEffect(Unit) {
        onDispose {
            controller.stop()
        }
    }
}
