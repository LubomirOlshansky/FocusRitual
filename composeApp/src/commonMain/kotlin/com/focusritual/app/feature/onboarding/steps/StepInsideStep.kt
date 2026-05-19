package com.focusritual.app.feature.onboarding.steps

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.theme.OrganicEasing
import com.focusritual.app.core.haptic.HapticController
import com.focusritual.app.feature.onboarding.components.AnimatedFadeIn
import com.focusritual.app.feature.onboarding.components.AtmosphericBackdrop
import com.focusritual.app.feature.onboarding.components.BreathingOrb
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.onboarding_step_inside_subtitle
import focusritual.composeapp.generated.resources.onboarding_step_inside_title
import focusritual.composeapp.generated.resources.onboarding_tap_the_light
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun StepInsideStep(
    hapticController: HapticController,
    onAdvance: () -> Unit,
) {
    var isExiting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val orbTapInteractionSource = remember { MutableInteractionSource() }

    val orbSize by animateDpAsState(
        targetValue = if (isExiting) 1100.dp else 240.dp,
        animationSpec = tween(1500, easing = OrganicEasing),
    )
    val orbIntensity by animateFloatAsState(
        targetValue = if (isExiting) 3.5f else 1f,
        animationSpec = tween(1500, easing = OrganicEasing),
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(500),
    )

    val breathTransition = rememberInfiniteTransition()
    val labelAlpha by breathTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val pulseTransition = rememberInfiniteTransition()
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val pulseColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericBackdrop(showForest = false, particleCount = 15, glowIntensity = 0.85f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 110.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedFadeIn(delayMs = 200, durationMs = 1800) {
                Text(
                    text = stringResource(Res.string.onboarding_step_inside_title),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.W300,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(16.dp))
            AnimatedFadeIn(delayMs = 800, durationMs = 2400) {
                Text(
                    text = stringResource(Res.string.onboarding_step_inside_subtitle),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f),
                    fontSize = 13.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.W300,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = pulseColor.copy(alpha = pulseAlpha * contentAlpha),
                radius = 120.dp.toPx() * pulseScale,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = 0.5.dp.toPx()),
            )
        }

        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = orbTapInteractionSource,
                ) {
                    if (!isExiting) {
                        hapticController.onboardingAdvance()
                        isExiting = true
                        scope.launch {
                            delay(900)
                            hapticController.onboardingAdvance()
                            onAdvance()
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            BreathingOrb(size = orbSize, intensity = orbIntensity)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = stringResource(Res.string.onboarding_tap_the_light),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = labelAlpha * contentAlpha),
                fontSize = 10.sp,
                fontWeight = FontWeight.W300,
                letterSpacing = 0.28.em,
            )
        }
    }
}
