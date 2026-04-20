package com.focusritual.app.feature.mixer.domain

import androidx.compose.runtime.Immutable

@Immutable
data class CurrentMixSummary(
    val isPlaying: Boolean,
    val activeSoundCount: Int,
    val activeSoundsSummary: String,
)

@Immutable
data class GroupedSounds(
    val byCategory: Map<SoundCategory, List<SoundState>>,
)
