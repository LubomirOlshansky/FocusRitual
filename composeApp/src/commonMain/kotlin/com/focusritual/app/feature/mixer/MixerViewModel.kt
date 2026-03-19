package com.focusritual.app.feature.mixer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MixerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MixerUiState())
    val uiState: StateFlow<MixerUiState> = _uiState.asStateFlow()

    fun onIntent(intent: MixerIntent) {
        when (intent) {
            MixerIntent.TogglePlayback -> {
                _uiState.update { it.copy(isPlaying = !it.isPlaying) }
            }
            is MixerIntent.ToggleSound -> {
                _uiState.update { state ->
                    state.copy(
                        sounds = state.sounds.map { sound ->
                            if (sound.id == intent.soundId) {
                                sound.copy(isEnabled = !sound.isEnabled)
                            } else {
                                sound
                            }
                        }
                    )
                }
            }
            is MixerIntent.AdjustVolume -> {
                _uiState.update { state ->
                    state.copy(
                        sounds = state.sounds.map { sound ->
                            if (sound.id == intent.soundId) {
                                sound.copy(volume = intent.volume.coerceIn(0f, 1f))
                            } else {
                                sound
                            }
                        }
                    )
                }
            }
        }
    }
}
