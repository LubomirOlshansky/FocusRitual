package com.focusritual.app.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.core.audio.SoundMixer
import com.focusritual.app.core.audio.SoundResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import focusritual.composeapp.generated.resources.Res

class MixerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MixerUiState())
    val uiState: StateFlow<MixerUiState> = _uiState.asStateFlow()

    private val soundMixer = SoundMixer()
    private val _sessionMasterVolume = MutableStateFlow<Float?>(null)

    init {
        loadSoundResources()
        _uiState.update { it.withDerivedFields() }
        viewModelScope.launch {
            combine(_uiState, _sessionMasterVolume) { state, sessionVolume ->
                Triple(
                    state.sounds,
                    if (sessionVolume != null) sessionVolume > 0.01f else state.isPlaying,
                    sessionVolume ?: 1f,
                )
            }.collect { (sounds, isPlaying, masterVolume) ->
                soundMixer.syncState(sounds, isPlaying, masterVolume)
            }
        }
    }

    fun setSessionMasterVolume(volume: Float?) {
        _sessionMasterVolume.value = volume
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
                _uiState.update { it.copy(isPlaying = !it.isPlaying).withDerivedFields() }
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
                    ).withDerivedFields()
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
                    ).withDerivedFields()
                }
            }
        }
    }

    private fun MixerUiState.withDerivedFields(): MixerUiState {
        val enabled = sounds.filter { it.isEnabled }
        val count = enabled.size
        val summary = when {
            count == 0 -> ""
            count <= 2 -> enabled.joinToString(" • ") { it.name }
            else -> enabled.take(2).joinToString(" • ") { it.name } + " • +${count - 2}"
        }
        return copy(activeSoundsSummary = summary, activeSoundCount = count)
    }

    override fun onCleared() {
        super.onCleared()
        soundMixer.release()
    }
}
