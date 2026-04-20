package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository
import com.focusritual.app.feature.mixer.domain.TestSoundCatalog
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToggleGlobalOrganicMotionUseCaseTest {
    @Test fun all_off_then_invoke_turns_active_on() {
        val repo = MixRepository(TestSoundCatalog())
        // Default seed: rain & wind enabled, both organicMotion=false.
        ToggleGlobalOrganicMotionUseCase(repo)()
        val after = repo.state.value
        assertTrue(after.first { it.id == "rain" }.organicMotion)
        assertTrue(after.first { it.id == "wind" }.organicMotion)
        // Disabled sound left alone.
        assertFalse(after.first { it.id == "cafe" }.organicMotion)
    }

    @Test fun any_on_then_invoke_turns_all_off() {
        val repo = MixRepository(TestSoundCatalog())
        ToggleOrganicMotionUseCase(repo)("rain") // turn rain organic on
        ToggleGlobalOrganicMotionUseCase(repo)()
        val after = repo.state.value
        assertFalse(after.first { it.id == "rain" }.organicMotion)
        assertFalse(after.first { it.id == "wind" }.organicMotion)
    }
}
