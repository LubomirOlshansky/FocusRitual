package com.focusritual.app.feature.mixer

data class MixerUiState(
    val isPlaying: Boolean = false,
    val sceneName: String = "Midnight Rain",
    val sceneSubtitle: String = "AETHER IMMERSION",
)

sealed interface MixerIntent {
    data object TogglePlayback : MixerIntent
}
