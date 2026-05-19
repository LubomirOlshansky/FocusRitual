package com.focusritual.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import com.focusritual.app.feature.onboarding.steps.PillarsStep
import com.focusritual.app.feature.onboarding.steps.StepInsideStep
import com.focusritual.app.feature.onboarding.steps.WelcomeStep

@Composable
fun OnboardingScreenContent(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
) {
    AnimatedContent(
        targetState = state.currentStep,
        transitionSpec = {
            (fadeIn(tween(900, easing = FastOutSlowInEasing)) +
                scaleIn(tween(900, easing = FastOutSlowInEasing), initialScale = 0.96f))
                .togetherWith(fadeOut(tween(500)))
        },
        label = "onboardingStep",
    ) { step ->
        when (step) {
            OnboardingStep.Welcome -> WelcomeStep { onIntent(OnboardingIntent.AdvanceStep) }
            OnboardingStep.StepInside -> StepInsideStep { onIntent(OnboardingIntent.AdvanceStep) }
            OnboardingStep.Pillars -> PillarsStep { onIntent(OnboardingIntent.CompleteOnboarding) }
        }
    }
}
