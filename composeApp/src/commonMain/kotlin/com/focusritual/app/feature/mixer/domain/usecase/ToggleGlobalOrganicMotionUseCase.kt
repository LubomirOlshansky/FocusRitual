package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository

class ToggleGlobalOrganicMotionUseCase(private val repo: MixRepository) {
    operator fun invoke() {
        repo.update { sounds ->
            val active = sounds.filter { it.isEnabled }
            val anyOrganicOn = active.any { it.organicMotion }
            sounds.map { s ->
                if (s.isEnabled) s.copy(organicMotion = !anyOrganicOn) else s
            }
        }
    }
}
