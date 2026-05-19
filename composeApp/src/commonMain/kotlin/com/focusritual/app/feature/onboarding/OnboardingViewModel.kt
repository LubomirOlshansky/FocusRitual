package com.focusritual.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusritual.app.core.audio.AudioCommand
import com.focusritual.app.core.audio.SoundMixer
import com.focusritual.app.core.haptic.HapticController
import com.focusritual.app.feature.mixer.data.AmbientSnapshot
import com.focusritual.app.feature.mixer.data.AmbientStateRepository
import com.focusritual.app.feature.mixer.domain.SavedSound
import com.focusritual.app.feature.onboarding.data.OnboardingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SOUND_WIND = "wind"
private const val SOUND_RAIN = "rain"
private const val WIND_TARGET = 0.18f
private const val RAIN_TARGET = 0.30f
private const val WIND_FADE_MS = 2000L
private const val RAIN_FADE_MS = 1500L
private const val FADE_STEP_MS = 50L

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val soundMixer: SoundMixer,
    private val hapticController: HapticController,
    private val ambientRepo: AmbientStateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _completedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val completedEvents: SharedFlow<Unit> = _completedEvents.asSharedFlow()

    private var windVolume: Float = 0f
    private var rainVolume: Float = 0f

    init {
        viewModelScope.launch {
            fadeVolume(SOUND_WIND, from = 0f, to = WIND_TARGET, durationMs = WIND_FADE_MS) { v ->
                windVolume = v
            }
        }
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.AdvanceStep -> advance()
            OnboardingIntent.CompleteOnboarding -> complete()
        }
    }

    private fun advance() {
        hapticController.onboardingAdvance()
        when (_uiState.value.currentStep) {
            OnboardingStep.Welcome -> {
                _uiState.update { it.copy(currentStep = OnboardingStep.StepInside) }
                viewModelScope.launch {
                    fadeVolume(SOUND_RAIN, from = 0f, to = RAIN_TARGET, durationMs = RAIN_FADE_MS) { v ->
                        rainVolume = v
                    }
                }
            }
            OnboardingStep.StepInside -> {
                _uiState.update { it.copy(currentStep = OnboardingStep.Pillars) }
            }
            OnboardingStep.Pillars -> complete()
        }
    }

    private fun complete() {
        if (_uiState.value.isAdvancing) return
        _uiState.update { it.copy(isAdvancing = true) }
        hapticController.onboardingComplete()
        ambientRepo.write(
            AmbientSnapshot(
                sounds = listOf(
                    SavedSound(id = SOUND_RAIN, volume = rainVolume.coerceAtLeast(RAIN_TARGET), organicMotion = false),
                    SavedSound(id = SOUND_WIND, volume = windVolume.coerceAtLeast(WIND_TARGET), organicMotion = false),
                ),
                loadedPresetId = null,
            ),
        )
        onboardingRepository.markCompleted()
        _completedEvents.tryEmit(Unit)
    }

    // Stepped fade — SoundMixer has no native fade API; loop emits AudioCommand snapshots.
    private suspend fun fadeVolume(
        soundId: String,
        from: Float,
        to: Float,
        durationMs: Long,
        onProgress: (Float) -> Unit,
    ) {
        val steps = (durationMs / FADE_STEP_MS).toInt().coerceAtLeast(1)
        val delta = (to - from) / steps
        var current = from
        soundMixer.syncState(listOf(AudioCommand(id = soundId, volume = current, enabled = true)))
        onProgress(current)
        repeat(steps) {
            delay(FADE_STEP_MS)
            current += delta
            soundMixer.syncState(listOf(AudioCommand(id = soundId, volume = current, enabled = true)))
            onProgress(current)
        }
        soundMixer.syncState(listOf(AudioCommand(id = soundId, volume = to, enabled = true)))
        onProgress(to)
    }
}
