package com.focusritual.app.feature.timer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.core.designsystem.theme.Primary
import kotlinx.coroutines.delay

private data class CompletionParticle(
    val x: Float,
    val y: Float,
    val durationMs: Int,
    val delayMs: Int,
    val peakAlpha: Float,
)

@Composable
fun SessionCompleteScreen(
    onReturnToMixer: () -> Unit,
    onStartAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // 1) Background glows
        val glowTransition = rememberInfiniteTransition(label = "glow")
        val outerScale by glowTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = FocusRitualEasing.Atmospheric),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "outerScale",
        )
        val outerAlpha by glowTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = FocusRitualEasing.Atmospheric),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "outerAlpha",
        )
        val innerScale by glowTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = FocusRitualEasing.Atmospheric),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(800),
            ),
            label = "innerScale",
        )
        val innerAlpha by glowTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = FocusRitualEasing.Atmospheric),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(800),
            ),
            label = "innerAlpha",
        )

        Box(
            modifier = Modifier
                .align(BiasAlignment(0f, -0.24f))
                .size(320.dp)
                .graphicsLayer {
                    scaleX = outerScale
                    scaleY = outerScale
                    alpha = outerAlpha
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.055f), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(BiasAlignment(0f, -0.24f))
                .size(180.dp)
                .graphicsLayer {
                    scaleX = innerScale
                    scaleY = innerScale
                    alpha = innerAlpha
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.07f), Color.Transparent),
                    ),
                ),
        )

        // 2) Particles layer
        val particles = remember {
            List(14) { i ->
                val rng = kotlin.random.Random(seed = i * 31 + 7)
                CompletionParticle(
                    x = 0.15f + rng.nextFloat() * 0.70f,
                    y = 0.25f + rng.nextFloat() * 0.45f,
                    durationMs = 10_000 + (rng.nextFloat() * 12_000).toInt(),
                    delayMs = (rng.nextFloat() * 8_000).toInt(),
                    peakAlpha = 0.15f + rng.nextFloat() * 0.25f,
                )
            }
        }
        val timeMs by produceState(0L) {
            while (true) {
                withFrameMillis { value = it }
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val translateMaxPx = 200.dp.toPx()
            val baseRadiusPx = 1.dp.toPx()
            particles.forEach { p ->
                val elapsed = timeMs - p.delayMs
                if (elapsed < 0) return@forEach
                val t = ((elapsed % p.durationMs).toFloat()) / p.durationMs
                val translateY = -translateMaxPx * t
                val scale = 1f - 0.7f * t
                val a = when {
                    t < 0.15f -> (t / 0.15f) * p.peakAlpha
                    t > 0.85f -> ((1f - t) / 0.15f) * p.peakAlpha
                    else -> p.peakAlpha
                }
                if (a <= 0f || scale <= 0f) return@forEach
                drawCircle(
                    color = Primary.copy(alpha = a),
                    radius = baseRadiusPx * scale,
                    center = Offset(p.x * size.width, p.y * size.height + translateY),
                )
            }
        }

        // 3) Center column
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center,
            ) {
                Ring(sizeDp = 140, delayMs = 100)
                Ring(sizeDp = 108, delayMs = 350)
                Ring(sizeDp = 78, delayMs = 600)
                CheckCircle()
            }

            Spacer(Modifier.height(48.dp))

            // Headline
            var headlineStarted by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(650)
                headlineStarted = true
            }
            val headlineT by animateFloatAsState(
                targetValue = if (headlineStarted) 1f else 0f,
                animationSpec = tween(900, easing = FocusRitualEasing.Ritual),
                label = "headlineT",
            )
            Text(
                text = "Well done.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.025).em,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    translationY = (1f - headlineT) * 12.dp.toPx()
                    alpha = headlineT
                },
            )

            Spacer(Modifier.height(10.dp))

            // Subline
            var sublineStarted by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(800)
                sublineStarted = true
            }
            val sublineT by animateFloatAsState(
                targetValue = if (sublineStarted) 1f else 0f,
                animationSpec = tween(900, easing = FocusRitualEasing.Ritual),
                label = "sublineT",
            )
            Text(
                text = "Your session is complete",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.02.em,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    translationY = (1f - sublineT) * 12.dp.toPx()
                    alpha = sublineT
                },
            )
        }

        // 4) Bottom block
        var ctaStarted by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(1100)
            ctaStarted = true
        }
        val ctaT by animateFloatAsState(
            targetValue = if (ctaStarted) 1f else 0f,
            animationSpec = tween(900, easing = FocusRitualEasing.Ritual),
            label = "ctaT",
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp)
                .graphicsLayer {
                    translationY = (1f - ctaT) * 12.dp.toPx()
                    alpha = ctaT
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(Primary.copy(alpha = 0.08f))
                    .border(0.5.dp, Primary.copy(alpha = 0.18f), RoundedCornerShape(27.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onReturnToMixer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "RETURN TO SANCTUARY",
                    fontSize = 12.sp,
                    letterSpacing = 0.14.em,
                    color = Primary.copy(alpha = 0.75f),
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Start another",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.04.em,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStartAnother,
                    )
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun Ring(sizeDp: Int, delayMs: Int) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        started = true
    }
    val t by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(2400, easing = FocusRitualEasing.Ritual),
        label = "ringT",
    )
    val scale = lerp(0.82f, 1f, t)
    val alpha = if (t < 0.25f) {
        lerp(0f, 0.20f, t / 0.25f)
    } else {
        lerp(0.20f, 0.07f, (t - 0.25f) / 0.75f)
    }
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(0.5.dp, Primary.copy(alpha = alpha), CircleShape),
    )
}

@Composable
private fun CheckCircle() {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        started = true
    }
    val t by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, easing = FocusRitualEasing.Ritual),
        label = "checkCircleT",
    )
    val scale = lerp(0.65f, 1f, t)

    var checkStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(900)
        checkStarted = true
    }
    val checkAlpha by animateFloatAsState(
        targetValue = if (checkStarted) 1f else 0f,
        animationSpec = tween(500, easing = LinearEasing),
        label = "checkAlpha",
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = t
            }
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.06f))
            .border(0.5.dp, Primary.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val s = size.width / 24f
            val path = Path().apply {
                moveTo(6f * s, 12.5f * s)
                lineTo(10.5f * s, 17f * s)
                lineTo(18f * s, 8f * s)
            }
            drawPath(
                path = path,
                color = Primary.copy(alpha = 0.80f * checkAlpha),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}
