package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository

class AdjustVolumeUseCase(private val repo: MixRepository) {
    operator fun invoke(soundId: String, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        repo.update { sounds ->
            sounds.map { s ->
                if (s.id == soundId) s.copy(volume = clamped) else s
            }
        }
    }
}
