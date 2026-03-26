package com.focusritual.app.feature.timer

enum class SessionPhase { Focus, Break }

data class ActiveSessionUiState(
    val phase: SessionPhase = SessionPhase.Focus,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val currentCycle: Int = 1,
    val totalCycles: Int = 4,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
) {
    val remainingFormatted: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "$minutes:${seconds.toString().padStart(2, '0')}"
        }

    val progress: Float
        get() = if (totalSeconds > 0) 1f - (remainingSeconds.toFloat() / totalSeconds) else 0f

    val phaseLabel: String
        get() = when {
            isCompleted -> "COMPLETE"
            phase == SessionPhase.Focus -> "FOCUS SESSION"
            else -> "BREAK"
        }
}

sealed interface ActiveSessionIntent {
    data object TogglePause : ActiveSessionIntent
    data object Stop : ActiveSessionIntent
    data object Skip : ActiveSessionIntent
}
