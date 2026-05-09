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

    private fun performIfEnabled(type: HapticFeedbackType) {
        if (!settingsRepository.hapticsEnabled.value) return
        engine.perform(type)
    }
}
