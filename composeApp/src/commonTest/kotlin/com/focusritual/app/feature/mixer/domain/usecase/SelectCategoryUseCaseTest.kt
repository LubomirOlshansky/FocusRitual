package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.SoundCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectCategoryUseCaseTest {
    @Test fun updates_flow_value() {
        val flow = MutableStateFlow(SoundCategory.ALL)
        SelectCategoryUseCase(flow)(SoundCategory.WEATHER)
        assertEquals(SoundCategory.WEATHER, flow.value)
        SelectCategoryUseCase(flow)(SoundCategory.NATURE)
        assertEquals(SoundCategory.NATURE, flow.value)
    }
}
