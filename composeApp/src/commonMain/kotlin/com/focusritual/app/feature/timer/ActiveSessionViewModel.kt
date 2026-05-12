package com.focusritual.app.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.core.haptic.HapticController
import com.focusritual.app.feature.session.SessionConfig
import com.focusritual.app.feature.session.SessionMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActiveSessionViewModel(
    private val config: SessionConfig,
    private val hapticController: HapticController = HapticController(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ActiveSessionUiState(
            sessionMode = config.mode,
            totalCycles = config.totalCycles,
        ),
    )
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var hasFiredSessionCompleteHaptic = false

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
            val fadeOutThreshold = if (config.mode == SessionMode.Sleep) {
                config.sleepFadeOutMinutes * 60
            } else {
                0
            }

            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                if (!_uiState.value.isPaused) {
                    val newRemaining = _uiState.value.remainingSeconds - 1
                    val shouldStartFade = config.mode == SessionMode.Sleep
                        && newRemaining <= fadeOutThreshold
                        && !_uiState.value.isSleepFadingOut
                    _uiState.update {
                        it.copy(
                            remainingSeconds = newRemaining,
                            isSleepFadingOut = it.isSleepFadingOut || shouldStartFade,
                        )
                    }
                }
            }
            onPhaseComplete(isNaturalCompletion = true)
        }
    }

    private fun onPhaseComplete(isNaturalCompletion: Boolean) {
        val state = _uiState.value
        if (state.isSleepMode) {
            timerJob?.cancel()
            if (isNaturalCompletion) {
                performSessionCompleteHapticOnce()
            }
            _uiState.update { it.copy(isCompleted = true, isSleepFadingOut = true, remainingSeconds = 0) }
            return
        }
        when (state.phase) {
            SessionPhase.Focus -> {
                if (state.currentCycle >= state.totalCycles) {
                    advanceCycle(isNaturalCompletion)
                } else if (config.breakMinutes > 0) {
                    startPhase(SessionPhase.Break, state.currentCycle)
                } else {
                    advanceCycle(isNaturalCompletion)
                }
            }
            SessionPhase.Break -> advanceCycle(isNaturalCompletion)
        }
    }

    private fun advanceCycle(isNaturalCompletion: Boolean) {
        val state = _uiState.value
        if (state.currentCycle >= state.totalCycles) {
            timerJob?.cancel()
            if (isNaturalCompletion) {
                performSessionCompleteHapticOnce()
            }
            _uiState.update { it.copy(isCompleted = true, remainingSeconds = 0) }
        } else {
            startPhase(SessionPhase.Focus, state.currentCycle + 1)
        }
    }

    private fun performSessionCompleteHapticOnce() {
        if (hasFiredSessionCompleteHaptic) return
        hasFiredSessionCompleteHaptic = true
        hapticController.sessionComplete()
    }

    fun onIntent(intent: ActiveSessionIntent) {
        when (intent) {
            ActiveSessionIntent.TogglePause -> {
                if (_uiState.value.isPaused) {
                    hapticController.sessionResumed()
                } else {
                    hapticController.sessionPaused()
                }
                _uiState.update { it.copy(isPaused = !it.isPaused) }
            }
            ActiveSessionIntent.Stop -> { /* handled by navigation callback */ }
            ActiveSessionIntent.Skip -> onPhaseComplete(isNaturalCompletion = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
