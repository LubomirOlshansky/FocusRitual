package com.focusritual.app.core.liveactivity

/**
 * Represents the 3 distinct presentation states for the iOS Live Activity.
 * Each state carries its own typed content model — no generic "one layout fits all".
 */
sealed interface LiveActivityState {

    val isPaused: Boolean
    val mixSummary: String

    /**
     * State 1 — Ambient playback only.
     * Shown when the ambient mix is playing with no focus or sleep session active.
     * Lightest and calmest visual treatment.
     */
    data class AmbientPlayback(
        override val mixSummary: String,
        val activeSoundCount: Int,
        override val isPaused: Boolean,
    ) : LiveActivityState

    /**
     * State 2 — Focus session active.
     * Strongest visual hierarchy: large countdown, phase, cycle indicator.
     */
    data class FocusActive(
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val phase: String,
        val currentCycle: Int,
        val totalCycles: Int,
        override val mixSummary: String,
        override val isPaused: Boolean,
    ) : LiveActivityState {
        val progress: Float
            get() = if (totalSeconds > 0) 1f - (remainingSeconds.toFloat() / totalSeconds) else 0f

        val remainingFormatted: String
            get() {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                return "$minutes:${seconds.toString().padStart(2, '0')}"
            }

        val cycleLabel: String
            get() = "Cycle $currentCycle of $totalCycles"
    }

    /**
     * State 3 — Sleep session active.
     * Softer, calmer than focus. No cycle info. Gentle and drifting.
     */
    data class SleepActive(
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val fadeOutMinutes: Int,
        override val mixSummary: String,
        override val isPaused: Boolean,
    ) : LiveActivityState {
        val remainingFormatted: String
            get() {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                return "$minutes:${seconds.toString().padStart(2, '0')}"
            }

        val fadeOutLabel: String
            get() = "Fade out in $fadeOutMinutes min"
    }
}
