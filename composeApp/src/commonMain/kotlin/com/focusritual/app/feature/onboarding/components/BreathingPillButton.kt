package com.focusritual.app.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.focusritual.app.core.designsystem.softBlur

@Composable
fun BreathingPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition()
    val haloScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val haloAlpha by transition.animateFloat(
        initialValue = 0.26f,
        targetValue = 0.54f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val pillScale by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
    )

    val primary = MaterialTheme.colorScheme.primary
    val pillBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val pillBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f)

    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(320.dp)) {
            drawCircle(
                color = primary.copy(alpha = pulseAlpha),
                radius = (size.minDimension / 2) * pulseScale,
                style = Stroke(width = 0.5.dp.toPx()),
            )
        }
        Box(
            Modifier
                .size(320.dp)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                }
                .softBlur(28.dp)
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to primary.copy(alpha = haloAlpha),
                            1f to Color.Transparent,
                        ),
                        radius = 400f,
                    ),
                ),
        )
        Box(
            Modifier
                .graphicsLayer {
                    scaleX = pillScale * pressScale
                    scaleY = pillScale * pressScale
                }
                .clip(RoundedCornerShape(28.dp))
                .background(pillBg)
                .border(0.5.dp, pillBorder, RoundedCornerShape(28.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        },
                        onTap = { onClick() },
                    )
                }
                .padding(horizontal = 52.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = labelColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.W300,
                letterSpacing = 0.22.em,
            )
        }
    }
}
