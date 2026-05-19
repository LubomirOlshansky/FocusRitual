package com.focusritual.app.feature.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AtmosphericBackdrop(
    modifier: Modifier = Modifier,
    showForest: Boolean = true,
    particleCount: Int = 5,
    glowIntensity: Float = 1f,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        if (showForest) {
            ForestKenBurnsLayer()
        }
        DriftingMistLayer()
        AmbientGlowPulse(intensity = glowIntensity)
        DriftingParticleField(count = particleCount)
        VignetteOverlay()
        FilmGrainOverlay()
    }
}
