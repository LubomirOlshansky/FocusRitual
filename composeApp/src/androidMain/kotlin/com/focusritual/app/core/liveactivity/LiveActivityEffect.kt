package com.focusritual.app.core.liveactivity

import androidx.compose.runtime.Composable
import com.focusritual.app.feature.mixer.MixerUiState
import com.focusritual.app.feature.timer.ActiveSessionUiState

/** Android: no-op — Live Activities are iOS-only. */
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
    // No-op on Android
}
