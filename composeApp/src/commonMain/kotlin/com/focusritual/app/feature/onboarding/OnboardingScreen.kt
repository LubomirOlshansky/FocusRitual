package com.focusritual.app.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.audio.SoundMixer
import com.focusritual.app.core.haptic.HapticController
import com.focusritual.app.feature.mixer.data.AmbientStateRepository
import com.focusritual.app.feature.onboarding.data.OnboardingRepository

@Composable
fun OnboardingScreen(
    onboardingRepository: OnboardingRepository,
    soundMixer: SoundMixer,
    hapticController: HapticController,
    ambientStateRepository: AmbientStateRepository,
    onComplete: () -> Unit,
) {
    val viewModel: OnboardingViewModel = viewModel {
        OnboardingViewModel(
            onboardingRepository = onboardingRepository,
            soundMixer = soundMixer,
            hapticController = hapticController,
            ambientRepo = ambientStateRepository,
        )
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.completedEvents.collect { onComplete() }
    }
    OnboardingScreenContent(state = state, hapticController = hapticController, onIntent = viewModel::onIntent)
}
