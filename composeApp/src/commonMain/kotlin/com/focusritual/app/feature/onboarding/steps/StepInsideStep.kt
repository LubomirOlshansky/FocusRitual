package com.focusritual.app.feature.onboarding.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.feature.onboarding.components.AtmosphericBackdrop
import com.focusritual.app.feature.onboarding.components.BreathingOrb
import com.focusritual.app.feature.onboarding.components.PulsingTapHint
import com.focusritual.app.feature.onboarding.components.StepIndicator

@Composable
fun StepInsideStep(onAdvance: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = interactionSource,
            ) { onAdvance() },
    ) {
        AtmosphericBackdrop(showForest = false, particleCount = 6, glowIntensity = 0.7f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 58.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            StepIndicator("— 2 / 3 —")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Step inside.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                fontSize = 22.sp,
                fontWeight = FontWeight.W300,
                letterSpacing = 0.005.em,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Breathe with the light.\nThis is the rhythm of every session.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f),
                fontSize = 11.sp,
                fontWeight = FontWeight.W300,
                lineHeight = 18.sp,
                letterSpacing = 0.04.em,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 60.dp),
            contentAlignment = Alignment.Center,
        ) {
            BreathingOrb(size = 180.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            PulsingTapHint("TAP TO CONTINUE")
        }
    }
}
