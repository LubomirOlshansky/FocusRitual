package com.focusritual.app.feature.mixer

import com.focusritual.app.feature.mixer.model.SoundState
import com.focusritual.app.feature.mixer.model.defaultSounds

data class MixerUiState(
    val isPlaying: Boolean = false,
    val sceneName: String = "Midnight Rain",
    val sceneSubtitle: String = "AETHER IMMERSION",
    val sounds: List<SoundState> = defaultSounds(),
)

sealed interface MixerIntent {
    data object TogglePlayback : MixerIntent
    data class ToggleSound(val soundId: String) : MixerIntent
    data class AdjustVolume(val soundId: String, val volume: Float) : MixerIntent
}
