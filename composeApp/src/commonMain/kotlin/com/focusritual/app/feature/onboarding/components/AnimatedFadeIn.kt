package com.focusritual.app.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedFadeIn(
    delayMs: Int = 0,
    durationMs: Int = 1000,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
    )
    Box(Modifier.graphicsLayer { this.alpha = alpha }) {
        content()
    }
}

@Composable
fun AnimatedFadeUp(
    delayMs: Int = 0,
    durationMs: Int = 1000,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
    )
    val offsetPx = with(LocalDensity.current) { 12.dp.toPx() }
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else offsetPx,
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing),
    )
    Box(
        Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translateY
        },
    ) {
        content()
    }
}
