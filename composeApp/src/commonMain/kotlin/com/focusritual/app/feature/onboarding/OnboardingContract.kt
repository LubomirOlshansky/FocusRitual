package com.focusritual.app.feature.onboarding

enum class OnboardingStep { Welcome, StepInside, Pillars }

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val isAdvancing: Boolean = false,
)

sealed interface OnboardingIntent {
    data object AdvanceStep : OnboardingIntent
    data object CompleteOnboarding : OnboardingIntent
}
