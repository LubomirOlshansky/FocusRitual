package com.focusritual.app.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.core.audio.OrganicMotionEngine
import com.focusritual.app.core.audio.SoundMixer
import com.focusritual.app.core.audio.SoundResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import focusritual.composeapp.generated.resources.Res

class MixerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MixerUiState())

    private val soundMixer = SoundMixer()
    private val organicEngine = OrganicMotionEngine(viewModelScope)
    private val _sessionMasterVolume = MutableStateFlow<Float?>(null)

    val uiState: StateFlow<MixerUiState> = combine(
        _uiState,
        organicEngine.offsets,
    ) { state, offsets ->
        if (offsets.isEmpty()) state
        else state.copy(
            sounds = state.sounds.map { sound ->
                sound.copy(liveVolume = offsets[sound.id])
            },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value)

    init {
        loadSoundResources()
        _uiState.update { it.withDerivedFields() }
        viewModelScope.launch {
            combine(
                _uiState,
                _sessionMasterVolume,
                organicEngine.offsets,
            ) { state, sessionVolume, offsets ->
                val adjustedSounds = state.sounds.map { sound ->
                    val effectiveVolume = offsets[sound.id] ?: sound.volume
                    sound.copy(volume = effectiveVolume)
                }
                Triple(
                    adjustedSounds,
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
                                val newEnabled = !sound.isEnabled
                                if (!newEnabled) {
                                    organicEngine.disable(sound.id)
                                } else if (sound.organicMotion) {
                                    organicEngine.enable(sound.id, sound.volume)
                                }
                                sound.copy(isEnabled = newEnabled)
                            } else {
                                sound
                            }
                        },
                    ).withDerivedFields()
                }
            }
            is MixerIntent.AdjustVolume -> {
                _uiState.update { state ->
                    state.copy(
                        sounds = state.sounds.map { sound ->
                            if (sound.id == intent.soundId) {
                                val clamped = intent.volume.coerceIn(0f, 1f)
                                if (sound.organicMotion) {
                                    organicEngine.updateBase(sound.id, clamped)
                                }
                                sound.copy(volume = clamped)
                            } else {
                                sound
                            }
                        },
                    ).withDerivedFields()
                }
            }
            is MixerIntent.ToggleOrganicMotion -> {
                _uiState.update { state ->
                    state.copy(
                        sounds = state.sounds.map { sound ->
                            if (sound.id == intent.soundId) {
                                val newEnabled = !sound.organicMotion
                                if (newEnabled && sound.isEnabled) {
                                    organicEngine.enable(sound.id, sound.volume)
                                } else {
                                    organicEngine.disable(sound.id)
                                }
                                sound.copy(organicMotion = newEnabled)
                            } else {
                                sound
                            }
                        },
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
        organicEngine.release()
        soundMixer.release()
    }
}
