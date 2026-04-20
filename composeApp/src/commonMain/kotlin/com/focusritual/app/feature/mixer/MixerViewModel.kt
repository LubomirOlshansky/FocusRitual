package com.focusritual.app.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.feature.mixer.domain.MixRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MixerViewModel(
    catalog: SoundCatalog = SoundCatalogImpl(),
    private val repo: MixRepository = MixRepository(catalog),
    private val orchestrator: MixAudioOrchestrator = MixAudioOrchestrator(catalog),
    private val toggleSound: ToggleSoundUseCase = ToggleSoundUseCase(repo),
    private val adjustVolume: AdjustVolumeUseCase = AdjustVolumeUseCase(repo),
    private val toggleOrganicMotion: ToggleOrganicMotionUseCase = ToggleOrganicMotionUseCase(repo),
    private val removeFromMix: RemoveFromMixUseCase = RemoveFromMixUseCase(repo),
    private val toggleGlobalOrganic: ToggleGlobalOrganicMotionUseCase = ToggleGlobalOrganicMotionUseCase(repo),
) : ViewModel() {

    private val _isPlaying = MutableStateFlow(true)
    private val _selectedCategory = MutableStateFlow(SoundCategory.ALL)
    private val _sessionMasterVolume = MutableStateFlow<Float?>(null)

    private val togglePlayback = TogglePlaybackUseCase(_isPlaying)
    private val selectCategory = SelectCategoryUseCase(_selectedCategory)

    val uiState: StateFlow<MixerUiState> = combine(
        repo.state,
        _isPlaying,
        _selectedCategory,
        orchestrator.offsets,
    ) { sounds, isPlaying, category, offsets ->
        val withLive = if (offsets.isEmpty()) sounds
                       else sounds.map { it.copy(liveVolume = offsets[it.id]) }
        val summary = summarizeActiveMix(withLive, isPlaying)
        MixerUiState(
            isPlaying = isPlaying,
            sounds = withLive,
            selectedCategory = category,
            activeSoundsSummary = summary.activeSoundsSummary,
            activeSoundCount = summary.activeSoundCount,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MixerUiState())

    val filteredSounds: StateFlow<Map<SoundCategory, List<SoundState>>> = uiState.map { state ->
        groupActiveSounds(state.sounds, state.selectedCategory).byCategory
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch { orchestrator.loadAll() }
        orchestrator.start(viewModelScope, repo.state, _isPlaying, _sessionMasterVolume)
    }

    fun onIntent(intent: MixerIntent) {
        when (intent) {
            MixerIntent.TogglePlayback -> togglePlayback()
            is MixerIntent.ToggleSound -> toggleSound(intent.soundId)
            is MixerIntent.AdjustVolume -> adjustVolume(intent.soundId, intent.volume)
            is MixerIntent.ToggleOrganicMotion -> toggleOrganicMotion(intent.soundId)
            is MixerIntent.RemoveFromMix -> removeFromMix(intent.soundId)
            MixerIntent.ToggleGlobalOrganicMotion -> toggleGlobalOrganic()
            is MixerIntent.SelectCategory -> selectCategory(intent.category)
        }
    }

    fun setSessionMasterVolume(volume: Float?) {
        _sessionMasterVolume.value = volume
    }

    override fun onCleared() {
        super.onCleared()
        orchestrator.release()
    }
}
