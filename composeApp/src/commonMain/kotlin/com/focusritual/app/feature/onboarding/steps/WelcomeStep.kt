package com.focusritual.app.feature.onboarding.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.softBlur
import com.focusritual.app.feature.onboarding.components.AnimatedFadeIn
import com.focusritual.app.feature.onboarding.components.AnimatedFadeUp
import com.focusritual.app.feature.onboarding.components.AtmosphericBackdrop
import com.focusritual.app.feature.onboarding.components.PulsingTapHint
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.onboarding_tap_to_continue
import focusritual.composeapp.generated.resources.onboarding_welcome_body
import focusritual.composeapp.generated.resources.onboarding_welcome_tagline
import focusritual.composeapp.generated.resources.onboarding_welcome_wordmark
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeStep(onAdvance: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hairlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
    val scrimColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = interactionSource,
            ) { onAdvance() },
    ) {
        AtmosphericBackdrop(showForest = true, particleCount = 5, glowIntensity = 1f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedFadeUp(delayMs = 0, durationMs = 2400) {
                Text(
                    text = stringResource(Res.string.onboarding_welcome_wordmark),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.W200,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(12.dp))
            AnimatedFadeIn(delayMs = 600, durationMs = 2400) {
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 0.5.dp)
                        .background(hairlineColor),
                )
            }
            Spacer(Modifier.height(14.dp))
            AnimatedFadeIn(delayMs = 800, durationMs = 3000) {
                Text(
                    text = stringResource(Res.string.onboarding_welcome_tagline),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W300,
                    letterSpacing = 0.32.em,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedFadeIn(delayMs = 1400, durationMs = 3000) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(width = 260.dp, height = 72.dp)
                            .softBlur(16.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        scrimColor.copy(alpha = 0.55f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                    Text(
                        text = stringResource(Res.string.onboarding_welcome_body),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.W300,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(36.dp))
            PulsingTapHint(stringResource(Res.string.onboarding_tap_to_continue))
        }
    }
}
