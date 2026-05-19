package com.focusritual.app.feature.onboarding.steps

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.feature.onboarding.components.AtmosphericBackdrop
import com.focusritual.app.feature.onboarding.components.ShimmerPillButton
import com.focusritual.app.feature.onboarding.components.StepIndicator
import kotlinx.coroutines.delay

@Composable
fun PillarsStep(onComplete: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphericBackdrop(showForest = true, particleCount = 3, glowIntensity = 0.8f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 58.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            StepIndicator("— 3 / 3 —")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 88.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "A small space,\nthree ways to inhabit it.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                fontSize = 17.sp,
                fontWeight = FontWeight.W300,
                lineHeight = 24.sp,
                letterSpacing = 0.01.em,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PillarCard(
                icon = Icons.Outlined.GraphicEq,
                name = "MIXER",
                description = "Layer ambient sounds into your own atmosphere.",
                entranceDelayMs = 400,
            )
            PillarCard(
                icon = Icons.Outlined.Schedule,
                name = "FOCUS",
                description = "Timed work sessions with breaks. Block distractions.",
                entranceDelayMs = 800,
            )
            PillarCard(
                icon = Icons.Outlined.Bedtime,
                name = "SLEEP",
                description = "A countdown that fades to silence as you drift off.",
                entranceDelayMs = 1200,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ShimmerPillButton(text = "ENTER", onClick = onComplete)
        }
    }
}

@Composable
private fun PillarCard(
    icon: ImageVector,
    name: String,
    description: String,
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

    val haloTransition = rememberInfiniteTransition()
    val haloScale by haloTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(entranceDelayMs),
        ),
    )
    val haloAlpha by haloTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(entranceDelayMs),
        ),
    )

    val bg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val border = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val iconBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val iconBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
    val haloBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY.toPx()
            }
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer {
                        scaleX = haloScale
                        scaleY = haloScale
                        this.alpha = haloAlpha
                    }
                    .clip(CircleShape)
                    .border(0.5.dp, haloBorder, CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(0.5.dp, iconBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                fontSize = 11.sp,
                fontWeight = FontWeight.W400,
                letterSpacing = 0.14.em,
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.W300,
                lineHeight = 16.sp,
            )
        }
    }
}
