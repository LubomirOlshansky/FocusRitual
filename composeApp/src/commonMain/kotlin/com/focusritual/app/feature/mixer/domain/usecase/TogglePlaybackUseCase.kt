package com.focusritual.app.feature.mixer.domain.usecase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TogglePlaybackUseCase(private val isPlayingFlow: MutableStateFlow<Boolean>) {
    operator fun invoke() {
        isPlayingFlow.update { !it }
    }
}
