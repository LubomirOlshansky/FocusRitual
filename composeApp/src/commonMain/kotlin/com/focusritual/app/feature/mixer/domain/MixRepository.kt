package com.focusritual.app.feature.mixer.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MixRepository(private val catalog: SoundCatalog) {

    private val _state = MutableStateFlow(seed())
    val state: StateFlow<List<SoundState>> = _state.asStateFlow()

    fun update(transform: (List<SoundState>) -> List<SoundState>) {
        _state.update(transform)
    }

    private fun seed(): List<SoundState> = catalog.all().map { def ->
        val (enabled, volume) = when (def.id) {
            "rain" -> true to 0.7f
            "wind" -> true to 0.5f
            else -> false to 0.5f
        }
        SoundState(
            id = def.id,
            name = def.name,
            icon = def.icon,
            category = def.category,
            isEnabled = enabled,
            volume = volume,
            organicMotion = false,
            liveVolume = null,
        )
    }
}
