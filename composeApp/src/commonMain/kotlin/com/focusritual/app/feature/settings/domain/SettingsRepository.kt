package com.focusritual.app.feature.settings.domain

import com.focusritual.app.core.audio.AudioSettingsRepository
import com.focusritual.app.core.haptic.HapticSettingsRepository

class SettingsRepository(
    private val audioSettingsRepository: AudioSettingsRepository = AudioSettingsRepository.Default,
    private val hapticSettingsRepository: HapticSettingsRepository = HapticSettingsRepository.Default,
) {
    val mixWithOthersEnabled = audioSettingsRepository.mixWithOthersEnabled
    val duckOthersEnabled = audioSettingsRepository.duckOthersEnabled
    val mixWithOthersVolume = audioSettingsRepository.mixWithOthersVolume
    val duckLevel = audioSettingsRepository.duckLevel
    val hapticsEnabled = hapticSettingsRepository.hapticsEnabled

    suspend fun setMixWithOthers(enabled: Boolean) {
        audioSettingsRepository.setMixWithOthers(enabled)
    }

    suspend fun setDuckOthers(enabled: Boolean) {
        audioSettingsRepository.setDuckOthers(enabled)
    }

    suspend fun setMixWithOthersVolume(volume: Float) {
        audioSettingsRepository.setMixWithOthersVolume(volume)
    }

    suspend fun setDuckLevel(level: Float) {
        audioSettingsRepository.setDuckLevel(level)
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        hapticSettingsRepository.setHapticsEnabled(enabled)
    }
}