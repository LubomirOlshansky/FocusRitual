package com.focusritual.app.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.core.haptic.HapticController
import com.focusritual.app.core.util.currentTimeMillis
import com.focusritual.app.feature.mixer.data.AmbientSnapshot
import com.focusritual.app.feature.mixer.data.AmbientStateRepository
import com.focusritual.app.feature.mixer.data.MixPresetRepository
import com.focusritual.app.feature.mixer.domain.MixPreset
import com.focusritual.app.feature.mixer.domain.MixRepository
import com.focusritual.app.feature.mixer.domain.SavedSound
import com.focusritual.app.feature.mixer.domain.SoundCatalog
import com.focusritual.app.feature.mixer.domain.SoundCatalogImpl
import com.focusritual.app.feature.mixer.domain.MixAudioOrchestrator
import com.focusritual.app.feature.mixer.domain.groupActiveSounds
import com.focusritual.app.feature.mixer.domain.summarizeActiveMix
import com.focusritual.app.feature.mixer.domain.usecase.AdjustVolumeUseCase
import com.focusritual.app.feature.mixer.domain.usecase.RemoveFromMixUseCase
import com.focusritual.app.feature.mixer.domain.usecase.SelectCategoryUseCase
import com.focusritual.app.feature.mixer.domain.usecase.ToggleGlobalOrganicMotionUseCase
import com.focusritual.app.feature.mixer.domain.usecase.ToggleOrganicMotionUseCase
import com.focusritual.app.feature.mixer.domain.usecase.TogglePlaybackUseCase
import com.focusritual.app.feature.mixer.domain.usecase.ToggleSoundUseCase
import com.focusritual.app.feature.mixer.domain.SoundCategory
import com.focusritual.app.feature.mixer.domain.SoundState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class MixerViewModel(
    catalog: SoundCatalog = SoundCatalogImpl(),
    private val presetRepo: MixPresetRepository = MixPresetRepository(),
    private val ambientRepo: AmbientStateRepository = AmbientStateRepository(),
    private val repo: MixRepository = MixRepository(catalog, ambientRepo.read()?.sounds),
    private val orchestrator: MixAudioOrchestrator = MixAudioOrchestrator(catalog),
    private val hapticController: HapticController = HapticController(),
    private val toggleSound: ToggleSoundUseCase = ToggleSoundUseCase(repo),
    private val adjustVolume: AdjustVolumeUseCase = AdjustVolumeUseCase(repo),
    private val toggleOrganicMotion: ToggleOrganicMotionUseCase = ToggleOrganicMotionUseCase(repo),
    private val removeFromMix: RemoveFromMixUseCase = RemoveFromMixUseCase(repo),
    private val toggleGlobalOrganic: ToggleGlobalOrganicMotionUseCase = ToggleGlobalOrganicMotionUseCase(repo),
) : ViewModel() {

    private val _isPlaying = MutableStateFlow(true)
    private val _selectedCategory = MutableStateFlow(SoundCategory.ALL)
    private val _sessionMasterVolume = MutableStateFlow<Float?>(null)
    private val _loadedPresetId: MutableStateFlow<String?>
    private val _isDirtyFromPreset = MutableStateFlow(false)

    private val togglePlayback = TogglePlaybackUseCase(_isPlaying)
    private val selectCategory = SelectCategoryUseCase(_selectedCategory)

    val uiState: StateFlow<MixerUiState>

    val filteredSounds: StateFlow<Map<SoundCategory, List<SoundState>>>

    init {
        // Hydrate loadedPresetId from snapshot, dropping ids not present in saved presets.
        val snapshot = ambientRepo.read()
        val validPresetId = snapshot?.loadedPresetId
            ?.takeIf { id -> presetRepo.state.value.any { it.id == id } }
        _loadedPresetId = MutableStateFlow(validPresetId)

        uiState = combine(
            repo.state,
            _isPlaying,
            _selectedCategory,
            orchestrator.offsets,
            presetRepo.state,
            _loadedPresetId,
            _isDirtyFromPreset,
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val sounds = values[0] as List<SoundState>
            val isPlaying = values[1] as Boolean
            val category = values[2] as SoundCategory
            @Suppress("UNCHECKED_CAST")
            val offsets = values[3] as Map<String, Float>
            @Suppress("UNCHECKED_CAST")
            val savedMixes = values[4] as List<MixPreset>
            val loadedPresetId = values[5] as String?
            val isDirtyFromPreset = values[6] as Boolean

            val withLive = if (offsets.isEmpty()) sounds
                           else sounds.map { it.copy(liveVolume = offsets[it.id]) }
            val summary = summarizeActiveMix(withLive, isPlaying)
            MixerUiState(
                isPlaying = isPlaying,
                sounds = withLive,
                selectedCategory = category,
                activeSoundsSummary = summary.activeSoundsSummary,
                activeSoundCount = summary.activeSoundCount,
                activeSoundNames = withLive.filter { it.isEnabled }.map { it.name },
                savedMixes = savedMixes,
                loadedPresetId = loadedPresetId,
                isDirtyFromPreset = isDirtyFromPreset,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, MixerUiState())

        filteredSounds = uiState.map { state ->
            groupActiveSounds(state.sounds, state.selectedCategory).byCategory
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

        viewModelScope.launch { orchestrator.loadAll() }
        orchestrator.start(viewModelScope, repo.state, _isPlaying, _sessionMasterVolume)

        // Debounced ambient persistence. Per the persistence rule:
        //   - If a preset is loaded AND not dirty -> persist with loadedPresetId.
        //   - Otherwise (no preset loaded, or preset loaded then tweaked) -> persist with null.
        // This means a tweaked preset reopens as a custom config (no active preset chip).
        combine(repo.state, _loadedPresetId, _isDirtyFromPreset) { sounds, pid, dirty ->
            Triple(sounds, pid, dirty)
        }
            .debounce(300)
            .onEach { (sounds, pid, dirty) ->
                val activeSaved = sounds
                    .filter { it.isEnabled }
                    .map { SavedSound(it.id, it.volume, it.organicMotion) }
                val persistedPid = if (pid != null && !dirty) pid else null
                ambientRepo.write(AmbientSnapshot(activeSaved, persistedPid))
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: MixerIntent) {
        when (intent) {
            MixerIntent.TogglePlayback -> togglePlayback()
            is MixerIntent.ToggleSound -> {
                val wasDisabled = repo.state.value.firstOrNull { it.id == intent.soundId }?.isEnabled == false
                markDirtyIfNeeded()
                toggleSound(intent.soundId)
                if (wasDisabled) {
                    hapticController.soundTileEnabled()
                }
            }
            is MixerIntent.AdjustVolume -> {
                markDirtyIfNeeded()
                adjustVolume(intent.soundId, intent.volume)
            }
            is MixerIntent.ToggleOrganicMotion -> {
                markDirtyIfNeeded()
                toggleOrganicMotion(intent.soundId)
            }
            is MixerIntent.RemoveFromMix -> {
                markDirtyIfNeeded()
                removeFromMix(intent.soundId)
            }
            MixerIntent.ToggleGlobalOrganicMotion -> {
                markDirtyIfNeeded()
                toggleGlobalOrganic()
            }
            is MixerIntent.SaveCurrentMix -> saveCurrentMix(intent.name)
            is MixerIntent.LoadMix -> loadMix(intent.presetId)
            is MixerIntent.DeleteMix -> deleteMix(intent.presetId)
            is MixerIntent.SelectCategory -> selectCategory(intent.category)
        }
    }

    private fun markDirtyIfNeeded() {
        if (_loadedPresetId.value != null && !_isDirtyFromPreset.value) {
            _isDirtyFromPreset.value = true
        }
    }

    private fun saveCurrentMix(name: String) {
        val activeSounds = repo.state.value.filter { it.isEnabled }
        val preset = MixPreset(
            id = generatePresetId(),
            name = name.trim(),
            sounds = activeSounds.map { SavedSound(it.id, it.volume, it.organicMotion) },
            createdAt = currentTimeMillis(),
        )
        presetRepo.save(preset)
        hapticController.mixSaved()
        _loadedPresetId.value = preset.id
        _isDirtyFromPreset.value = false
    }

    private fun deleteMix(presetId: String) {
        presetRepo.delete(presetId)
        if (_loadedPresetId.value == presetId) {
            _loadedPresetId.value = null
            _isDirtyFromPreset.value = false
        }
    }

    private fun loadMix(presetId: String) {
        val preset = presetRepo.state.value.firstOrNull { it.id == presetId } ?: return
        repo.loadSnapshot(preset.sounds)
        _loadedPresetId.value = preset.id
        _isDirtyFromPreset.value = false
        hapticController.mixLoaded()
    }

    private fun generatePresetId(): String =
        "${currentTimeMillis()}_${Random.nextInt()}"

    fun setSessionMasterVolume(volume: Float?) {
        _sessionMasterVolume.value = volume
    }

    override fun onCleared() {
        super.onCleared()
        orchestrator.release()
    }
}
