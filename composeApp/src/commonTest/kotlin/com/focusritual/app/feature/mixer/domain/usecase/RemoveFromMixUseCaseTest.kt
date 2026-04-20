package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository
import com.focusritual.app.feature.mixer.domain.TestSoundCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RemoveFromMixUseCaseTest {
    @Test fun sets_isEnabled_false_on_matching_only() {
        val repo = MixRepository(TestSoundCatalog())
        val rainVolBefore = repo.state.value.first { it.id == "rain" }.volume
        val wind = repo.state.value.first { it.id == "wind" }
        RemoveFromMixUseCase(repo)("rain")
        val after = repo.state.value
        assertFalse(after.first { it.id == "rain" }.isEnabled)
        // Volume preserved (current behaviour: only isEnabled flipped to false).
        assertEquals(rainVolBefore, after.first { it.id == "rain" }.volume)
        // Other sound untouched.
        assertEquals(wind, after.first { it.id == "wind" })
    }
}
