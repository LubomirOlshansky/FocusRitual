package com.focusritual.app.feature.mixer.domain.usecase

import com.focusritual.app.feature.mixer.domain.SoundCategory
import kotlinx.coroutines.flow.MutableStateFlow

class SelectCategoryUseCase(private val selectedCategoryFlow: MutableStateFlow<SoundCategory>) {
    operator fun invoke(category: SoundCategory) {
        selectedCategoryFlow.value = category
    }
}
