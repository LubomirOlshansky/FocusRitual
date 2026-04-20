package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository

class ToggleOrganicMotionUseCase(private val repo: MixRepository) {
    operator fun invoke(soundId: String) {
        repo.update { sounds ->
            sounds.map { s ->
                if (s.id == soundId) s.copy(organicMotion = !s.organicMotion) else s
            }
        }
    }
}
