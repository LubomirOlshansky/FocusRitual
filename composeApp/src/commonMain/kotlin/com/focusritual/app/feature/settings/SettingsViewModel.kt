package com.focusritual.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.feature.onboarding.data.OnboardingRepository
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

private data class PartialSettings(
    val mixWithOthersEnabled: Boolean,
    val duckOthersEnabled: Boolean,
    val mixWithOthersVolume: Float,
    val duckLevel: Float,
    val hapticsEnabled: Boolean,
)

class SettingsViewModel(
    private val repository: SettingsRepository = SettingsRepository(),
    private val onboardingRepository: OnboardingRepository = OnboardingRepository(),
) : ViewModel() {
    private val activeDetail = MutableStateFlow<SettingsDetail?>(null)
    private val _effects = MutableSharedFlow<SettingsEffect>(extraBufferCapacity = 1)

    val effects = _effects.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            repository.mixWithOthersEnabled,
            repository.duckOthersEnabled,
            repository.mixWithOthersVolume,
            repository.duckLevel,
            repository.hapticsEnabled,
        ) { mixWithOthers, duckOthers, mixVolume, duckLvl, haptics ->
            PartialSettings(mixWithOthers, duckOthers, mixVolume, duckLvl, haptics)
        },
        activeDetail,
        onboardingRepository.hasCompletedFlow,
    ) { partial, detail, onboardingCompleted ->
        SettingsUiState(
            mixWithOthersEnabled = partial.mixWithOthersEnabled,
            duckOthersEnabled = partial.duckOthersEnabled,
            mixWithOthersVolume = partial.mixWithOthersVolume,
            duckLevel = partial.duckLevel,
            hapticsEnabled = partial.hapticsEnabled,
            activeDetail = detail,
            onboardingCompleted = onboardingCompleted,
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
            is SettingsIntent.SetOnboardingCompleted -> viewModelScope.launch {
                if (intent.completed) onboardingRepository.markCompleted()
                else onboardingRepository.reset()
            }
        }
    }

    private fun emitEffect(effect: SettingsEffect) {
        _effects.tryEmit(effect)
    }
}