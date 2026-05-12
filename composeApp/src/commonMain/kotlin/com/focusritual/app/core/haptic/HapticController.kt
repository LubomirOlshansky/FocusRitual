package com.focusritual.app.core.haptic

class HapticController(
    private val settingsRepository: HapticSettingsRepository = HapticSettingsRepository.Default,
    private val engine: HapticEngine = PlatformHapticEngine(),
) {
    fun soundTileEnabled() {
        performIfEnabled(HapticFeedbackType.LightImpact)
    }

    fun mixLoaded() {
        performIfEnabled(HapticFeedbackType.MediumImpact)
    }

    fun sessionStarted() {
        performIfEnabled(HapticFeedbackType.Success)
    }

    fun hapticsEnabled() {
        engine.perform(HapticFeedbackType.LightImpact)
    }

    fun mixSaved() {
        performIfEnabled(HapticFeedbackType.LightImpact)
    }

    fun sessionComplete() {
        performIfEnabled(HapticFeedbackType.Success)
    }

    fun sessionPaused() {
        performIfEnabled(HapticFeedbackType.LightImpact)
    }

    fun sessionResumed() {
        performIfEnabled(HapticFeedbackType.LightImpact)
    }

    private fun performIfEnabled(type: HapticFeedbackType) {
        if (!settingsRepository.hapticsEnabled.value) return
        engine.perform(type)
    }
}
