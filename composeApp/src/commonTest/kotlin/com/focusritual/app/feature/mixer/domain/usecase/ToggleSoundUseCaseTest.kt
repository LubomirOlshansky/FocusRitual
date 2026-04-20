package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository
import com.focusritual.app.feature.mixer.domain.TestSoundCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToggleSoundUseCaseTest {
    @Test fun toggle_flips_matching_sound_only() {
        val repo = MixRepository(TestSoundCatalog())
        val useCase = ToggleSoundUseCase(repo)

        val before = repo.state.value
        val rainBefore = before.first { it.id == "rain" }.isEnabled
        val windBefore = before.first { it.id == "wind" }.isEnabled

        useCase("rain")
        val after = repo.state.value
        assertEquals(!rainBefore, after.first { it.id == "rain" }.isEnabled)
        assertEquals(windBefore, after.first { it.id == "wind" }.isEnabled)
    }

    @Test fun toggle_twice_returns_to_original() {
        val repo = MixRepository(TestSoundCatalog())
        val useCase = ToggleSoundUseCase(repo)
        val original = repo.state.value.first { it.id == "rain" }.isEnabled
        useCase("rain")
        useCase("rain")
        assertEquals(original, repo.state.value.first { it.id == "rain" }.isEnabled)
    }

    @Test fun toggle_unknown_id_no_change() {
        val repo = MixRepository(TestSoundCatalog())
        val before = repo.state.value
        ToggleSoundUseCase(repo)("not_a_sound")
        assertEquals(before, repo.state.value)
        assertTrue(true) // sanity
        assertFalse(false)
    }
}
