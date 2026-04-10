package com.focusritual.app.feature.mixer

import com.focusritual.app.feature.mixer.model.SoundState
import com.focusritual.app.feature.mixer.model.defaultSounds

data class MixerUiState(
    val isPlaying: Boolean = true,
    val sounds: List<SoundState> = defaultSounds(),
    val activeSoundsSummary: String = "",
    val activeSoundCount: Int = 0,
)

sealed interface MixerIntent {
    data object TogglePlayback : MixerIntent
    data class ToggleSound(val soundId: String) : MixerIntent
    data class AdjustVolume(val soundId: String, val volume: Float) : MixerIntent
    data class ToggleOrganicMotion(val soundId: String) : MixerIntent
    data class RemoveFromMix(val soundId: String) : MixerIntent
    data object ToggleGlobalOrganicMotion : MixerIntent
}
