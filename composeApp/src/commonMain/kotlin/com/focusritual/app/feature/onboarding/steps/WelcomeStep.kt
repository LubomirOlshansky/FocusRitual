package com.focusritual.app.feature.onboarding.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import com.focusritual.app.feature.onboarding.components.AnimatedFadeIn
import com.focusritual.app.feature.onboarding.components.AnimatedFadeUp
import com.focusritual.app.feature.onboarding.components.AtmosphericBackdrop
import com.focusritual.app.feature.onboarding.components.PulsingTapHint

@Composable
fun WelcomeStep(onAdvance: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = interactionSource,
            ) { onAdvance() },
    ) {
        AtmosphericBackdrop(showForest = true, particleCount = 5, glowIntensity = 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-30).dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedFadeUp(delayMs = 0, durationMs = 2400) {
                Text(
                    text = "FocusRitual",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.W200,
                    letterSpacing = 0.03.em,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedFadeIn(delayMs = 800, durationMs = 3000) {
                Text(
                    text = "A QUIET SPACE",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W300,
                    letterSpacing = 0.32.em,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 130.dp, start = 32.dp, end = 32.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedFadeIn(delayMs = 1400, durationMs = 3000) {
                Text(
                    text = "An ambient companion\nfor focused work and restful sleep.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W300,
                    lineHeight = 22.sp,
                    letterSpacing = 0.02.em,
                    textAlign = TextAlign.Center,
                )
            }
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
