package com.focusritual.app.core.liveactivity

/**
 * Handler protocol for the Swift side to implement.
 * Mirrors the ScreenTimeHandler pattern — Kotlin defines the interface,
 * Swift provides the implementation, wired at app startup.
 */
interface LiveActivityHandler {
    /**
     * Start a new Live Activity with the given session type and initial content.
     * @param sessionType one of "ambient", "focus", "sleep"
     */
    fun startActivity(
        sessionType: String,
        isPaused: Boolean,
        mixSummary: String,
        activeSoundCount: Int,
        remainingSeconds: Int,
        totalSeconds: Int,
        phase: String,
        currentCycle: Int,
        totalCycles: Int,
        fadeOutMinutes: Int,
    )

    /** Update the running Live Activity with new content. */
    fun updateActivity(
        isPaused: Boolean,
        mixSummary: String,
        activeSoundCount: Int,
        remainingSeconds: Int,
        totalSeconds: Int,
        phase: String,
        currentCycle: Int,
        totalCycles: Int,
        fadeOutMinutes: Int,
    )

    /** End the current Live Activity. */
    fun endActivity()
}

/**
 * Singleton bridge — same pattern as ScreenTimeBridge.
 * Registered once at app startup from iOSApp.swift.
 */
object LiveActivityBridge {
    var handler: LiveActivityHandler? = null

    /**
     * Callback invoked when a Live Activity button is tapped.
     * Set by the composable that owns session/mixer state.
     * Actions: "togglePause", "stopMix", "endSession"
     */
    var onAction: ((String) -> Unit)? = null

    /** Called from Swift when a Darwin notification arrives. */
    fun handleAction(action: String) {
        onAction?.invoke(action)
    }
}
