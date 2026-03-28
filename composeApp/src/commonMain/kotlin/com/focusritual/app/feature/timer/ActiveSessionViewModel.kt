package com.focusritual.app.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.feature.session.SessionConfig
import com.focusritual.app.feature.session.SessionMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActiveSessionViewModel(private val config: SessionConfig) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ActiveSessionUiState(
            sessionMode = config.mode,
            totalCycles = config.totalCycles,
        ),
    )
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        if (config.mode == SessionMode.Sleep) {
            startSleepSession()
        } else {
            startPhase(SessionPhase.Focus, cycle = 1)
        }
    }

    private fun startSleepSession() {
        val totalSeconds = config.sleepDurationMinutes * 60
        _uiState.update {
            it.copy(
                phase = SessionPhase.Focus,
                remainingSeconds = totalSeconds,
                totalSeconds = totalSeconds,
                currentCycle = 1,
                isPaused = false,
                isCompleted = false,
            )
        }
        startTimer()
    }

    private fun startPhase(phase: SessionPhase, cycle: Int) {
        val totalSeconds = when (phase) {
            SessionPhase.Focus -> config.focusMinutes * 60
            SessionPhase.Break -> config.breakMinutes * 60
        }
        _uiState.update {
            it.copy(
                phase = phase,
                remainingSeconds = totalSeconds,
                totalSeconds = totalSeconds,
                currentCycle = cycle,
                isPaused = false,
                isCompleted = false,
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                if (!_uiState.value.isPaused) {
                    _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                }
            }
            onPhaseComplete()
        }
    }

    private fun onPhaseComplete() {
        val state = _uiState.value
        if (state.isSleepMode) {
            timerJob?.cancel()
            _uiState.update { it.copy(isCompleted = true, isSleepFadingOut = true, remainingSeconds = 0) }
            return
        }
        when (state.phase) {
            SessionPhase.Focus -> {
                if (config.breakMinutes > 0) {
                    startPhase(SessionPhase.Break, state.currentCycle)
                } else {
                    advanceCycle()
                }
            }
            SessionPhase.Break -> advanceCycle()
        }
    }

    private fun advanceCycle() {
        val state = _uiState.value
        if (state.currentCycle >= state.totalCycles) {
            timerJob?.cancel()
            _uiState.update { it.copy(isCompleted = true, remainingSeconds = 0) }
        } else {
            startPhase(SessionPhase.Focus, state.currentCycle + 1)
        }
    }

    fun onIntent(intent: ActiveSessionIntent) {
        when (intent) {
            ActiveSessionIntent.TogglePause -> {
                _uiState.update { it.copy(isPaused = !it.isPaused) }
            }
            ActiveSessionIntent.Stop -> { /* handled by navigation callback */ }
            ActiveSessionIntent.Skip -> onPhaseComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
