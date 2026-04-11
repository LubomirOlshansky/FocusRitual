package com.focusritual.app.core.liveactivity

import androidx.compose.runtime.Composable
import com.focusritual.app.feature.mixer.MixerUiState
import com.focusritual.app.feature.timer.ActiveSessionUiState

/**
 * Platform-aware composable that syncs the Live Activity with current app state.
 * iOS: pushes updates to ActivityKit. Android: no-op.
 *
 * @param mixerState current mixer state (always available)
 * @param sessionState active session state, or null if no session is running
 * @param isSessionActive whether a timer session is currently on screen
 */
@Composable
expect fun LiveActivityEffect(
    mixerState: MixerUiState,
    sessionState: ActiveSessionUiState?,
    isSessionActive: Boolean,
)
