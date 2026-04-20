package com.focusritual.app.feature.mixer.domain

import com.focusritual.app.core.audio.AudioCommand
import com.focusritual.app.core.audio.OrganicMotionEngine
import com.focusritual.app.core.audio.SoundMixer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns SoundMixer + OrganicMotionEngine. Replaces the audio-sync `combine`
 * that lived in MixerViewModel and moves all organic-engine side-effects
 * (`enable`/`disable`/`updateBase`) out of `MutableStateFlow.update {}` lambdas
 * — they now run inside `collect { }` bodies, which is the deliberate
 * latent-bug fix flagged by Phase 4.
 */

class MixAudioOrchestrator(private val catalog: SoundCatalog) {

    private val soundMixer = SoundMixer()
    private lateinit var organicEngine: OrganicMotionEngine

    private val _offsets = MutableStateFlow<Map<String, Float>>(emptyMap())
    val offsets: StateFlow<Map<String, Float>> = _offsets.asStateFlow()

    fun start(
        scope: CoroutineScope,
        sounds: StateFlow<List<SoundState>>,
        isPlaying: StateFlow<Boolean>,
        sessionMasterVolume: StateFlow<Float?>,
    ) {
        organicEngine = OrganicMotionEngine(scope)

        // Mirror engine offsets into the stable flow exposed since construction.
        scope.launch {
            organicEngine.offsets.collect { _offsets.value = it }
        }

        // Audio sync — equivalent to the original VM `combine(_uiState, _sessionMasterVolume, organicEngine.offsets)` block.
        scope.launch {
            combine(
                sounds,
                isPlaying,
                sessionMasterVolume,
                organicEngine.offsets,
            ) { state, playing, sessionVolume, offsets ->
                val adjustedSounds = state.map { sound ->
                    val effectiveVolume = offsets[sound.id] ?: sound.volume
                    sound.copy(volume = effectiveVolume)
                }
                Triple(
                    adjustedSounds,
                    if (sessionVolume != null) sessionVolume > 0.01f else playing,
                    sessionVolume ?: 1f,
                )
            }.collect { (s, playing, mv) ->
                val commands = s.map { sound ->
                    AudioCommand(
                        id = sound.id,
                        volume = sound.volume * mv,
                        enabled = playing && sound.isEnabled,
                    )
                }
                soundMixer.syncState(commands)
            }
        }

        // Organic-engine lifecycle — diff-based reaction to repo.state. Side-effects are
        // invoked exactly once per emission inside `collect { }` (NOT inside `update {}`).
        scope.launch {
            var prev: List<SoundState> = emptyList()
            sounds.collect { current ->
                val prevById = prev.associateBy { it.id }
                for (s in current) {
                    val p = prevById[s.id]
                    val wasOrganicActive = p != null && p.isEnabled && p.organicMotion
                    val isOrganicActive = s.isEnabled && s.organicMotion
                    when {
                        isOrganicActive && !wasOrganicActive ->
                            organicEngine.enable(s.id, s.volume)
                        !isOrganicActive && wasOrganicActive ->
                            organicEngine.disable(s.id)
                        isOrganicActive && wasOrganicActive && p.volume != s.volume ->
                            organicEngine.updateBase(s.id, s.volume)
                    }
                }
                prev = current
            }
        }
    }

    suspend fun loadAll() {
        for (def in catalog.all()) {
            val bytes = catalog.loadAudioBytes(def) ?: continue
            soundMixer.cacheAudioData(def.id, bytes)
        }
    }

    fun release() {
        if (::organicEngine.isInitialized) organicEngine.release()
        soundMixer.release()
    }
}
