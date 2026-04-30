package com.focusritual.app.feature.mixer.data

import com.focusritual.app.feature.mixer.domain.MixPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MixPresetRepository(private val store: JsonStore = JsonStore()) {

    private val _state = MutableStateFlow(load())
    val state: StateFlow<List<MixPreset>> = _state.asStateFlow()

    fun save(preset: MixPreset) {
        _state.update { it + preset }
        persist()
    }

    fun delete(id: String) {
        _state.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    fun nameExists(name: String, excludingId: String? = null): Boolean {
        val n = name.trim().lowercase()
        return _state.value.any { it.name.trim().lowercase() == n && it.id != excludingId }
    }

    private fun load(): List<MixPreset> = store.read<List<MixPreset>>(KEY) ?: emptyList()

    private fun persist() {
        store.write(KEY, _state.value)
    }

    companion object {
        private const val KEY = "mixer.saved_mixes.v1"
    }
}
