package com.focusritual.app.core.liveactivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.focusritual.app.feature.mixer.MixerUiState
import com.focusritual.app.feature.session.SessionMode
import com.focusritual.app.feature.timer.ActiveSessionUiState
import com.focusritual.app.feature.timer.SessionPhase

@Composable
actual fun LiveActivityEffect(
    mixerState: MixerUiState,
    sessionState: ActiveSessionUiState?,
    isSessionActive: Boolean,
) {
    val controller = remember { LiveActivityController() }

    LaunchedEffect(isSessionActive, sessionState, mixerState.isPlaying, mixerState.activeSoundsSummary) {
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

            // Ambient playback (no session, mix is playing)
            !isSessionActive && mixerState.isPlaying && mixerState.activeSoundCount > 0 -> {
                controller.push(
                    LiveActivityState.AmbientPlayback(
                        mixSummary = mixerState.activeSoundsSummary,
                        activeSoundCount = mixerState.activeSoundCount,
                        isPaused = false,
                    ),
                )
            }

            // Nothing active — stop the Live Activity
            else -> {
                controller.stop()
            }
        }
    }

    // Clean up when leaving composition
    DisposableEffect(Unit) {
        onDispose { controller.stop() }
    }
}
