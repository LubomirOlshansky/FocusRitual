package com.focusritual.app.feature.mixer.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MixRepository(
    private val catalog: SoundCatalog,
    initialSnapshot: List<SavedSound>? = null,
) {

    private val _state = MutableStateFlow(hydrate(initialSnapshot))
    val state: StateFlow<List<SoundState>> = _state.asStateFlow()

    fun update(transform: (List<SoundState>) -> List<SoundState>) {
        _state.update(transform)
    }

    fun loadSnapshot(snapshot: List<SavedSound>) {
        _state.value = hydrate(snapshot)
    }

    private fun hydrate(snapshot: List<SavedSound>?): List<SoundState> {
        if (snapshot.isNullOrEmpty()) return seed()
        val byId = snapshot.associateBy { it.id }
        return catalog.all().map { def ->
            val saved = byId[def.id]
            SoundState(
                id = def.id,
                name = def.name,
                icon = def.icon,
                category = def.category,
                isEnabled = saved != null,
                volume = saved?.volume ?: 0.5f,
                organicMotion = saved?.organicMotion ?: false,
                liveVolume = null,
            )
        }
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
