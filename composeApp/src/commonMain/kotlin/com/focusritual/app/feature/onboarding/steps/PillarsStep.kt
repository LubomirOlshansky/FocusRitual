package com.focusritual.app.feature.onboarding.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.theme.OrganicEasing
import com.focusritual.app.feature.onboarding.components.AnimatedFadeIn
import com.focusritual.app.feature.onboarding.components.AtmosphericBackdrop
import com.focusritual.app.feature.onboarding.components.BreathingPillButton
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.onboarding_enter
import focusritual.composeapp.generated.resources.onboarding_pillar_atmosphere_desc
import focusritual.composeapp.generated.resources.onboarding_pillar_atmosphere_name
import focusritual.composeapp.generated.resources.onboarding_pillar_focus_desc
import focusritual.composeapp.generated.resources.onboarding_pillar_focus_name
import focusritual.composeapp.generated.resources.onboarding_pillar_rest_desc
import focusritual.composeapp.generated.resources.onboarding_pillar_rest_name
import focusritual.composeapp.generated.resources.onboarding_pillar_ritual_desc
import focusritual.composeapp.generated.resources.onboarding_pillar_ritual_name
import focusritual.composeapp.generated.resources.onboarding_pillars_headline
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun PillarsStep(onComplete: () -> Unit) {
    val entryGlow = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        entryGlow.animateTo(0f, tween(2200, easing = OrganicEasing))
    }
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    var enterVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1700)
        enterVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericBackdrop(showForest = true, particleCount = 3, glowIntensity = 0.45f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val glow = entryGlow.value
            val radius = size.minDimension * 0.7f * (1.2f + (1f - glow) * 0.6f)
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to primary.copy(alpha = 0.35f * glow),
                        0.5f to primary.copy(alpha = 0.08f * glow),
                        1f to Color.Transparent,
                    ),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 88.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedFadeIn(delayMs = 200, durationMs = 1800) {
                Text(
                    text = stringResource(Res.string.onboarding_pillars_headline),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.W300,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 160.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                surface.copy(alpha = 0.55f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PillarCard(
                    name = stringResource(Res.string.onboarding_pillar_atmosphere_name),
                    description = stringResource(Res.string.onboarding_pillar_atmosphere_desc),
                    icon = Icons.Outlined.GraphicEq,
                    entranceDelayMs = 400,
                )
                PillarCard(
                    name = stringResource(Res.string.onboarding_pillar_ritual_name),
                    description = stringResource(Res.string.onboarding_pillar_ritual_desc),
                    icon = Icons.Outlined.SelfImprovement,
                    entranceDelayMs = 700,
                )
                PillarCard(
                    name = stringResource(Res.string.onboarding_pillar_focus_name),
                    description = stringResource(Res.string.onboarding_pillar_focus_desc),
                    icon = Icons.Outlined.Schedule,
                    entranceDelayMs = 1000,
                )
                PillarCard(
                    name = stringResource(Res.string.onboarding_pillar_rest_name),
                    description = stringResource(Res.string.onboarding_pillar_rest_desc),
                    icon = Icons.Outlined.Bedtime,
                    entranceDelayMs = 1300,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = enterVisible,
                enter = fadeIn(tween(1200)),
            ) {
                BreathingPillButton(
                    label = stringResource(Res.string.onboarding_enter),
                    onClick = onComplete,
                )
            }
        }
    }
}

@Composable
private fun PillarCard(
    name: String,
    description: String,
    icon: ImageVector,
    entranceDelayMs: Int,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(entranceDelayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 8.dp,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
    )

    val bg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
    val iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
    val nameColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
    val descColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY)
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(0.5.dp, borderColor, RoundedCornerShape(16.dp))
            .drawWithContent {
                drawContent()
                drawLine(
                    color = highlight,
                    start = Offset(20f, 0.5f),
                    end = Offset(size.width - 20f, 0.5f),
                    strokeWidth = 1f,
                )
            }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                color = nameColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.W400,
                letterSpacing = 0.12.em,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = description,
                color = descColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.W300,
            )
        }
    }
}
