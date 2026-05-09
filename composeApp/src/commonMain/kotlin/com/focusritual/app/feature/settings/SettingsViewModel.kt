package com.focusritual.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository = SettingsRepository(),
) : ViewModel() {
    private val activeDetail = MutableStateFlow<SettingsDetail?>(null)
    private val _effects = MutableSharedFlow<SettingsEffect>(extraBufferCapacity = 1)

    val effects = _effects.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.mixWithOthersEnabled,
        repository.duckOthersEnabled,
        repository.mixWithOthersVolume,
        repository.duckLevel,
        repository.hapticsEnabled,
        activeDetail,
    ) { values ->
        val mixWithOthersEnabled = values[0] as Boolean
        val duckOthersEnabled = values[1] as Boolean
        val mixWithOthersVolume = values[2] as Float
        val duckLevel = values[3] as Float
        val hapticsEnabled = values[4] as Boolean
        val activeDetail = values[5] as SettingsDetail?

        SettingsUiState(
            mixWithOthersEnabled = mixWithOthersEnabled,
            duckOthersEnabled = duckOthersEnabled,
            mixWithOthersVolume = mixWithOthersVolume,
            duckLevel = duckLevel,
            hapticsEnabled = hapticsEnabled,
            activeDetail = activeDetail,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetMixWithOthers -> viewModelScope.launch {
                repository.setMixWithOthers(intent.enabled)
            }
            is SettingsIntent.SetDuckOthers -> viewModelScope.launch {
                repository.setDuckOthers(intent.enabled)
            }
            is SettingsIntent.SetMixWithOthersVolume -> viewModelScope.launch {
                repository.setMixWithOthersVolume(intent.volume)
            }
            is SettingsIntent.SetDuckLevel -> viewModelScope.launch {
                repository.setDuckLevel(intent.level)
            }
            is SettingsIntent.SetHapticsEnabled -> viewModelScope.launch {
                repository.setHapticsEnabled(intent.enabled)
            }
            SettingsIntent.OpenLanguageSettings -> emitEffect(SettingsEffect.OpenLanguageSettings)
            SettingsIntent.RateApp -> emitEffect(SettingsEffect.RateApp)
            SettingsIntent.ShareApp -> emitEffect(SettingsEffect.ShareApp)
            SettingsIntent.ContactSupport -> emitEffect(SettingsEffect.ContactSupport())
            SettingsIntent.OpenSoundCredits -> activeDetail.update { SettingsDetail.SoundCredits }
            SettingsIntent.OpenPrivacyPolicy -> activeDetail.update { SettingsDetail.PrivacyPolicy }
            SettingsIntent.OpenTermsOfUse -> activeDetail.update { SettingsDetail.TermsOfUse }
            SettingsIntent.CloseDetail -> activeDetail.update { null }
            SettingsIntent.ResetToHome -> activeDetail.update { null }
        }
    }

    private fun emitEffect(effect: SettingsEffect) {
        _effects.tryEmit(effect)
    }
}