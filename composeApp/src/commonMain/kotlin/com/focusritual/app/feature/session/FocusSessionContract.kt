package com.focusritual.app.feature.session

enum class SessionMode { Focus, Sleep }

data class SessionPreset(
    val id: String,
    val label: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val sessions: Int,
)

fun defaultPresets() = listOf(
    SessionPreset("short", "25 min focus · 5 min break", 25, 5, 4),
    SessionPreset("medium", "50 min focus · 10 min break", 50, 10, 4),
    SessionPreset("long", "90 min deep focus", 90, 0, 1),
)

data class FocusSessionUiState(
    val presets: List<SessionPreset> = defaultPresets(),
    val selectedPresetId: String? = null,
    val customFocusMinutes: Int = 25,
    val customBreakMinutes: Int = 5,
    val customSessions: Int = 4,
    val sleepDurationMinutes: Int = 45,
    val sleepFadeOutMinutes: Int = 10,
) {
    val isCustomSelected: Boolean get() = selectedPresetId == null
}

sealed interface FocusSessionIntent {
    data class SelectPreset(val presetId: String) : FocusSessionIntent
    data object SelectCustom : FocusSessionIntent
    data class AdjustFocus(val delta: Int) : FocusSessionIntent
    data class AdjustBreak(val delta: Int) : FocusSessionIntent
    data class AdjustSessions(val delta: Int) : FocusSessionIntent
    data class AdjustSleepDuration(val delta: Int) : FocusSessionIntent
    data class AdjustSleepFadeOut(val delta: Int) : FocusSessionIntent
    data object StartSession : FocusSessionIntent
    data object Close : FocusSessionIntent
}

data class SessionConfig(
    val mode: SessionMode = SessionMode.Focus,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val totalCycles: Int,
    val sleepDurationMinutes: Int = 0,
    val sleepFadeOutMinutes: Int = 0,
)
