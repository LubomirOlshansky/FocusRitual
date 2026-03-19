package com.focusritual.app.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.core.audio.SoundMixer
import com.focusritual.app.core.audio.SoundResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import focusritual.composeapp.generated.resources.Res

class MixerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MixerUiState())
    val uiState: StateFlow<MixerUiState> = _uiState.asStateFlow()

    private val soundMixer = SoundMixer()

    init {
        loadSoundResources()
        viewModelScope.launch {
            _uiState.collect { state ->
                soundMixer.syncState(state.sounds, state.isPlaying)
            }
        }
    }

    private fun loadSoundResources() {
        viewModelScope.launch {
            for (sound in _uiState.value.sounds) {
                val path = SoundResources.getResourcePath(sound.id) ?: continue
                try {
                    val bytes = Res.readBytes(path)
                    soundMixer.cacheAudioData(sound.id, bytes)
                } catch (_: Exception) {
                    // Sound file not available — skip silently
                }
            }
        }
    }

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

    override fun onCleared() {
        super.onCleared()
        soundMixer.release()
    }
}
