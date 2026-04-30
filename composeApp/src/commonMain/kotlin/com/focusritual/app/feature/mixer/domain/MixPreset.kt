package com.focusritual.app.feature.mixer.domain

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class MixPreset(
    val id: String,
    val name: String,
    val sounds: List<SavedSound>,
    val createdAt: Long,
)

@Immutable
@Serializable
data class SavedSound(
    val id: String,
    val volume: Float,
    val organicMotion: Boolean,
)
