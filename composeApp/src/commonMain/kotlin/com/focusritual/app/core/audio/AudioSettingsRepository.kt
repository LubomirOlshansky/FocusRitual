package com.focusritual.app.core.audio

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

data class AudioPlaybackSettings(
    val mixWithOthersEnabled: Boolean = false,
    val duckOthersEnabled: Boolean = false,
    val mixWithOthersVolume: Float = AudioSettingsRepository.DefaultMixWithOthersVolume,
    val duckLevel: Float = AudioSettingsRepository.DefaultDuckLevel,
)

class AudioSettingsRepository(private val settings: Settings = Settings()) {
    private val _mixWithOthersEnabled = MutableStateFlow(settings.getBoolean(MixWithOthersKey, false))
    val mixWithOthersEnabled: StateFlow<Boolean> = _mixWithOthersEnabled.asStateFlow()

    private val _duckOthersEnabled = MutableStateFlow(settings.getBoolean(DuckOthersKey, false))
    val duckOthersEnabled: StateFlow<Boolean> = _duckOthersEnabled.asStateFlow()

    private val _mixWithOthersVolume = MutableStateFlow(
        settings.getFloat(MixWithOthersVolumeKey, DefaultMixWithOthersVolume).coerceIn(0f, 1f),
    )
    val mixWithOthersVolume: StateFlow<Float> = _mixWithOthersVolume.asStateFlow()

    private val _duckLevel = MutableStateFlow(
        settings.getFloat(DuckLevelKey, DefaultDuckLevel).coerceIn(0.10f, 0.70f),
    )
    val duckLevel: StateFlow<Float> = _duckLevel.asStateFlow()

    // Runtime attenuation multiplier set by platform audio focus/interruption controllers
    private val _externalAudioAttenuation = MutableStateFlow(1f)
    val externalAudioAttenuation: StateFlow<Float> = _externalAudioAttenuation.asStateFlow()

    val playbackSettings: Flow<AudioPlaybackSettings> = combine(
        mixWithOthersEnabled,
        duckOthersEnabled,
        mixWithOthersVolume,
        duckLevel,
    ) { mixWithOthersEnabled, duckOthersEnabled, mixWithOthersVolume, duckLevel ->
        AudioPlaybackSettings(
            mixWithOthersEnabled = mixWithOthersEnabled,
            duckOthersEnabled = duckOthersEnabled,
            mixWithOthersVolume = mixWithOthersVolume,
            duckLevel = duckLevel,
        )
    }

    suspend fun setMixWithOthers(enabled: Boolean) {
        settings.putBoolean(MixWithOthersKey, enabled)
        _mixWithOthersEnabled.value = enabled
    }

    suspend fun setDuckOthers(enabled: Boolean) {
        settings.putBoolean(DuckOthersKey, enabled)
        _duckOthersEnabled.value = enabled
    }

    suspend fun setMixWithOthersVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        settings.putFloat(MixWithOthersVolumeKey, clampedVolume)
        _mixWithOthersVolume.value = clampedVolume
    }

    suspend fun setDuckLevel(level: Float) {
        val clampedLevel = level.coerceIn(0.10f, 0.70f)
        settings.putFloat(DuckLevelKey, clampedLevel)
        _duckLevel.value = clampedLevel
    }

    fun setExternalAudioAttenuation(multiplier: Float) {
        _externalAudioAttenuation.value = multiplier.coerceIn(0f, 1f)
    }

    companion object {
        val Default = AudioSettingsRepository()

        const val MixWithOthersKey = "mix_with_others"
        const val DuckOthersKey = "duck_others"
        const val MixWithOthersVolumeKey = "mix_with_others_volume"
        const val DuckLevelKey = "duck_level"
        const val DefaultMixWithOthersVolume = 0.5f
        const val DefaultDuckLevel = 0.30f
    }
}