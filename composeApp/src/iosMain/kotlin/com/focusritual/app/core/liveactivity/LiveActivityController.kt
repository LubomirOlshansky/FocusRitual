package com.focusritual.app.core.liveactivity

/**
 * High-level controller that maps typed LiveActivityState into bridge calls.
 * Call from ViewModels or app-level coordinators when state changes.
 */
class LiveActivityController {

    private var currentSessionType: String? = null

    /** Start or update the Live Activity based on the current state. */
    fun push(state: LiveActivityState) {
        val handler = LiveActivityBridge.handler ?: return

        val newSessionType = when (state) {
            is LiveActivityState.AmbientPlayback -> "ambient"
            is LiveActivityState.FocusActive -> "focus"
            is LiveActivityState.SleepActive -> "sleep"
        }

        if (currentSessionType != null && currentSessionType != newSessionType) {
            handler.endActivity()
            currentSessionType = null
        }

        if (currentSessionType == null) {
            start(handler, state)
            currentSessionType = newSessionType
        } else {
            update(handler, state)
        }
    }

    /** End the Live Activity. */
    fun stop() {
        val handler = LiveActivityBridge.handler ?: return
        if (currentSessionType != null) {
            handler.endActivity()
            currentSessionType = null
        }
    }

    private fun start(handler: LiveActivityHandler, state: LiveActivityState) {
        val sessionType = when (state) {
            is LiveActivityState.AmbientPlayback -> "ambient"
            is LiveActivityState.FocusActive -> "focus"
            is LiveActivityState.SleepActive -> "sleep"
        }
        handler.startActivity(
            sessionType = sessionType,
            isPaused = state.isPaused,
            mixSummary = state.mixSummary,
            activeSoundCount = (state as? LiveActivityState.AmbientPlayback)?.activeSoundCount ?: 0,
            remainingSeconds = when (state) {
                is LiveActivityState.FocusActive -> state.remainingSeconds
                is LiveActivityState.SleepActive -> state.remainingSeconds
                else -> 0
            },
            totalSeconds = when (state) {
                is LiveActivityState.FocusActive -> state.totalSeconds
                is LiveActivityState.SleepActive -> state.totalSeconds
                else -> 0
            },
            phase = (state as? LiveActivityState.FocusActive)?.phase ?: "",
            currentCycle = (state as? LiveActivityState.FocusActive)?.currentCycle ?: 0,
            totalCycles = (state as? LiveActivityState.FocusActive)?.totalCycles ?: 0,
            fadeOutMinutes = (state as? LiveActivityState.SleepActive)?.fadeOutMinutes ?: 0,
        )
    }

    private fun update(handler: LiveActivityHandler, state: LiveActivityState) {
        handler.updateActivity(
            isPaused = state.isPaused,
            mixSummary = state.mixSummary,
            activeSoundCount = (state as? LiveActivityState.AmbientPlayback)?.activeSoundCount ?: 0,
            remainingSeconds = when (state) {
                is LiveActivityState.FocusActive -> state.remainingSeconds
                is LiveActivityState.SleepActive -> state.remainingSeconds
                else -> 0
            },
            totalSeconds = when (state) {
                is LiveActivityState.FocusActive -> state.totalSeconds
                is LiveActivityState.SleepActive -> state.totalSeconds
                else -> 0
            },
            phase = (state as? LiveActivityState.FocusActive)?.phase ?: "",
            currentCycle = (state as? LiveActivityState.FocusActive)?.currentCycle ?: 0,
            totalCycles = (state as? LiveActivityState.FocusActive)?.totalCycles ?: 0,
            fadeOutMinutes = (state as? LiveActivityState.SleepActive)?.fadeOutMinutes ?: 0,
        )
    }
}
