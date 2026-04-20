package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository
import com.focusritual.app.feature.mixer.domain.TestSoundCatalog
import kotlin.test.Test
import kotlin.test.assertEquals

class ToggleOrganicMotionUseCaseTest {
    @Test fun toggles_only_matching_sound() {
        val repo = MixRepository(TestSoundCatalog())
        val windBefore = repo.state.value.first { it.id == "wind" }.organicMotion
        val rainBefore = repo.state.value.first { it.id == "rain" }.organicMotion
        ToggleOrganicMotionUseCase(repo)("rain")
        val after = repo.state.value
        assertEquals(!rainBefore, after.first { it.id == "rain" }.organicMotion)
        assertEquals(windBefore, after.first { it.id == "wind" }.organicMotion)
    }
}
