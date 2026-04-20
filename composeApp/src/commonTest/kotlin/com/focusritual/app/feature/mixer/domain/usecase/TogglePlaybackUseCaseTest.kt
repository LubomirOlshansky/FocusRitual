package com.focusritual.app.feature.mixer.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class TogglePlaybackUseCaseTest {
    @Test fun flips_playing_flag() {
        val flow = MutableStateFlow(false)
        val useCase = TogglePlaybackUseCase(flow)
        useCase()
        assertEquals(true, flow.value)
        useCase()
        assertEquals(false, flow.value)
    }
}
