package com.focusritual.app.feature.session

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FocusSessionViewModel(
    private val prefs: SessionPreferences = SessionPreferences(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        FocusSessionUiState(
            customFocusMinutes = prefs.customFocusMinutes,
            customBreakMinutes = prefs.customBreakMinutes,
            customSessions = prefs.customSessions,
            sleepDurationMinutes = prefs.sleepDurationMinutes,
            sleepFadeOutMinutes = prefs.sleepFadeOutMinutes,
        )
    )
    val uiState: StateFlow<FocusSessionUiState> = _uiState.asStateFlow()

    fun onIntent(intent: FocusSessionIntent) {
        when (intent) {
            is FocusSessionIntent.SelectPreset -> {
                _uiState.update { it.copy(selectedPresetId = intent.presetId) }
            }
            FocusSessionIntent.SelectCustom -> {
                _uiState.update { it.copy(selectedPresetId = null) }
            }
            is FocusSessionIntent.AdjustFocus -> {
                _uiState.update {
                    it.copy(customFocusMinutes = (it.customFocusMinutes + intent.delta * 5).coerceIn(5, 120))
                }
                prefs.customFocusMinutes = _uiState.value.customFocusMinutes
            }
            is FocusSessionIntent.AdjustBreak -> {
                _uiState.update {
                    it.copy(customBreakMinutes = (it.customBreakMinutes + intent.delta).coerceIn(1, 30))
                }
                prefs.customBreakMinutes = _uiState.value.customBreakMinutes
            }
            is FocusSessionIntent.AdjustSessions -> {
                _uiState.update {
                    it.copy(customSessions = (it.customSessions + intent.delta).coerceIn(1, 10))
                }
                prefs.customSessions = _uiState.value.customSessions
            }
            is FocusSessionIntent.AdjustSleepDuration -> {
                _uiState.update { state ->
                    val newDuration = (state.sleepDurationMinutes + intent.delta * 5).coerceIn(15, 480)
                    val newFadeOut = state.sleepFadeOutMinutes.coerceAtMost(newDuration)
                    state.copy(sleepDurationMinutes = newDuration, sleepFadeOutMinutes = newFadeOut)
                }
                prefs.sleepDurationMinutes = _uiState.value.sleepDurationMinutes
                prefs.sleepFadeOutMinutes = _uiState.value.sleepFadeOutMinutes
            }
            is FocusSessionIntent.AdjustSleepFadeOut -> {
                _uiState.update { state ->
                    val newFadeOut = (state.sleepFadeOutMinutes + intent.delta * 5).coerceIn(0, state.sleepDurationMinutes)
                    state.copy(sleepFadeOutMinutes = newFadeOut)
                }
                prefs.sleepFadeOutMinutes = _uiState.value.sleepFadeOutMinutes
            }
            FocusSessionIntent.StartSession -> { /* handled by navigation callback */ }
            FocusSessionIntent.Close -> { /* handled by navigation callback */ }
        }
    }

    fun resolveConfig(): SessionConfig {
        val state = _uiState.value
        val preset = state.selectedPresetId?.let { id ->
            state.presets.find { it.id == id }
        }
        return if (preset != null) {
            SessionConfig(focusMinutes = preset.focusMinutes, breakMinutes = preset.breakMinutes, totalCycles = preset.sessions)
        } else {
            SessionConfig(focusMinutes = state.customFocusMinutes, breakMinutes = state.customBreakMinutes, totalCycles = state.customSessions)
        }
    }
}
