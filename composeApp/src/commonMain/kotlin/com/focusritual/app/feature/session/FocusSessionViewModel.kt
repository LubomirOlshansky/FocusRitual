package com.focusritual.app.feature.session

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FocusSessionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FocusSessionUiState())
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
            }
            is FocusSessionIntent.AdjustBreak -> {
                _uiState.update {
                    it.copy(customBreakMinutes = (it.customBreakMinutes + intent.delta).coerceIn(1, 30))
                }
            }
            is FocusSessionIntent.AdjustSessions -> {
                _uiState.update {
                    it.copy(customSessions = (it.customSessions + intent.delta).coerceIn(1, 10))
                }
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
            SessionConfig(preset.focusMinutes, preset.breakMinutes, preset.sessions)
        } else {
            SessionConfig(state.customFocusMinutes, state.customBreakMinutes, state.customSessions)
        }
    }
}
