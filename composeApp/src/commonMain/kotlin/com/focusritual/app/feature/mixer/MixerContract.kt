package com.focusritual.app.feature.mixer

import com.focusritual.app.feature.mixer.domain.SoundCategory
import com.focusritual.app.feature.mixer.domain.SoundState

data class MixerUiState(
    val isPlaying: Boolean = true,
    val sounds: List<SoundState> = emptyList(),
    val selectedCategory: SoundCategory = SoundCategory.ALL,
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
    data class SelectCategory(val category: SoundCategory) : MixerIntent
}
