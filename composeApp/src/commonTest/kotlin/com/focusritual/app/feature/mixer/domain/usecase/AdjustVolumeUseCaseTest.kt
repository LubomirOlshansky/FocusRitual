package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.MixRepository
import com.focusritual.app.feature.mixer.domain.TestSoundCatalog
import kotlin.test.Test
import kotlin.test.assertEquals

class AdjustVolumeUseCaseTest {
    @Test fun adjusts_only_matching_sound() {
        val repo = MixRepository(TestSoundCatalog())
        val windBefore = repo.state.value.first { it.id == "wind" }.volume
        AdjustVolumeUseCase(repo)("rain", 0.42f)
        val after = repo.state.value
        assertEquals(0.42f, after.first { it.id == "rain" }.volume)
        assertEquals(windBefore, after.first { it.id == "wind" }.volume)
    }

    @Test fun clamps_above_one() {
        val repo = MixRepository(TestSoundCatalog())
        AdjustVolumeUseCase(repo)("rain", 5f)
        assertEquals(1f, repo.state.value.first { it.id == "rain" }.volume)
    }

    @Test fun clamps_below_zero() {
        val repo = MixRepository(TestSoundCatalog())
        AdjustVolumeUseCase(repo)("rain", -1f)
        assertEquals(0f, repo.state.value.first { it.id == "rain" }.volume)
    }
}
