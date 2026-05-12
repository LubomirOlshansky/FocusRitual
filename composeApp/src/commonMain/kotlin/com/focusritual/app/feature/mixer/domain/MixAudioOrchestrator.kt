package com.focusritual.app.feature.mixer.domain

import com.focusritual.app.core.audio.AudioCommand
import com.focusritual.app.core.audio.AudioPlaybackSettings
import com.focusritual.app.core.audio.AudioSettingsRepository
import com.focusritual.app.core.audio.OrganicMotionEngine
import com.focusritual.app.core.audio.SoundMixer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private data class AudioSyncState(
    val sounds: List<SoundState>,
    val playing: Boolean,
    val sessionMasterVolume: Float,
    val playbackSettings: AudioPlaybackSettings,
    val externalAttenuation: Float,
)


class MixAudioOrchestrator(
    private val catalog: SoundCatalog,
    private val audioSettingsRepository: AudioSettingsRepository = AudioSettingsRepository.Default,
) {

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
                audioSettingsRepository.playbackSettings,
                audioSettingsRepository.externalAudioAttenuation,
                audioSettingsRepository.resumeTick,
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val state = values[0] as List<SoundState>
                val playing = values[1] as Boolean
                val sessionVolume = values[2] as Float?
                @Suppress("UNCHECKED_CAST")
                val offsets = values[3] as Map<String, Float>
                val playbackSettings = values[4] as AudioPlaybackSettings
                val externalAttenuation = values[5] as Float
                // values[6] is resumeTick — only included so combine re-emits to force
                // SoundMixer.syncState to re-issue play() on currently-enabled sounds
                // after an iOS interruption-ended ShouldResume.
                val adjustedSounds = state.map { sound ->
                    val effectiveVolume = offsets[sound.id] ?: sound.volume
                    sound.copy(volume = effectiveVolume)
                }
                AudioSyncState(
                    sounds = adjustedSounds,
                    playing = if (sessionVolume != null) sessionVolume > 0.01f else playing,
                    sessionMasterVolume = sessionVolume ?: 1f,
                    playbackSettings = playbackSettings,
                    externalAttenuation = externalAttenuation,
                )
            }.collect { syncState ->
                // Volume reduction when other audio is playing comes solely from
                // externalAudioAttenuation (driven by iOS silence-hint observer / polling
                // applying duckLevel). Enabling "Use while media is playing" by itself
                // must NOT lower volume.
                val commands = syncState.sounds.map { sound ->
                    AudioCommand(
                        id = sound.id,
                        volume = sound.volume * syncState.sessionMasterVolume * syncState.externalAttenuation,
                        enabled = syncState.playing && sound.isEnabled,
                    )
                }
                soundMixer.syncState(commands)
            }
        }

        // Organic-engine lifecycle — diff-based reaction to repo.state. Side-effects are
        // invoked exactly once per emission inside `collect { }` (NOT inside `update { }`).
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
