package com.focusritual.app.feature.mixer.domain

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

enum class SoundCategory { ALL, NATURE, WEATHER, PLACES, NOISE }

val SoundCategory.displayName: String
    get() = when (this) {
        SoundCategory.ALL -> "All"
        SoundCategory.NATURE -> "Nature"
        SoundCategory.WEATHER -> "Weather"
        SoundCategory.PLACES -> "Places"
        SoundCategory.NOISE -> "Noise"
    }

@Immutable
data class SoundState(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val category: SoundCategory,
    val isEnabled: Boolean = false,
    val volume: Float = 0.5f,
    val organicMotion: Boolean = false,
    val liveVolume: Float? = null,
)
