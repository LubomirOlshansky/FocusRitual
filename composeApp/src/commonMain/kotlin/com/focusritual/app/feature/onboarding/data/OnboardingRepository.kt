package com.focusritual.app.feature.onboarding.data

import com.focusritual.app.feature.mixer.data.JsonStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

class OnboardingRepository(private val store: JsonStore = JsonStore()) {

    private val _hasCompleted = MutableStateFlow(load().completed)
    val hasCompletedFlow: StateFlow<Boolean> = _hasCompleted.asStateFlow()

    fun markCompleted() {
        if (_hasCompleted.value) return
        _hasCompleted.value = true
        store.write(KEY, State(completed = true))
    }

    fun reset() {
        _hasCompleted.value = false
        store.write(KEY, State(completed = false))
    }

    private fun load(): State = store.read<State>(KEY) ?: State()

    @Serializable
    private data class State(val completed: Boolean = false)

    companion object {
        private const val KEY = "onboarding.state.v1"
    }
}
