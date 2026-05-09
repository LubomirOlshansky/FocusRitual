package com.focusritual.app.core.haptic

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HapticSettingsRepository(private val settings: Settings = Settings()) {
    private val _hapticsEnabled = MutableStateFlow(settings.getBoolean(HapticsEnabledKey, DefaultHapticsEnabled))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    suspend fun setHapticsEnabled(enabled: Boolean) {
        settings.putBoolean(HapticsEnabledKey, enabled)
        _hapticsEnabled.value = enabled
    }

    companion object {
        val Default = HapticSettingsRepository()

        const val HapticsEnabledKey = "haptic_feedback_enabled"
        const val DefaultHapticsEnabled = true
    }
}
