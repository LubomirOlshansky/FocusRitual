package com.focusritual.app.feature.timer

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.feature.session.SessionConfig
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.background
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun ActiveSessionScreen(
    config: SessionConfig,
    onFinish: () -> Unit,
    onSoundControl: (Float?) -> Unit,
    viewModel: ActiveSessionViewModel = viewModel { ActiveSessionViewModel(config) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isExiting by remember { mutableStateOf(false) }

    // Determine target volume based on phase and pause state
    val targetVolume = when {
        isExiting || uiState.isCompleted -> 0f
        uiState.isPaused -> 0f
        uiState.phase == SessionPhase.Focus -> 1f
        else -> 0f // Break phase — silent
    }

    val animatedVolume by animateFloatAsState(
        targetValue = targetVolume,
        animationSpec = tween(400),
    )

    // Push volume changes to the mixer
    LaunchedEffect(animatedVolume) {
        onSoundControl(animatedVolume)
    }

    // Fade out then navigate on exit
    LaunchedEffect(isExiting) {
        if (isExiting) {
            delay(450L) // Wait for fade-out animation
            onFinish()
        }
    }

    // Auto-exit after session completes
    if (uiState.isCompleted) {
        LaunchedEffect(Unit) {
            delay(2000L)
            isExiting = true
        }
    }

    // Release sound control when leaving composition
    DisposableEffect(Unit) {
        onDispose { onSoundControl(null) }
    }

    ActiveSessionScreenContent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                ActiveSessionIntent.Stop -> isExiting = true
                else -> viewModel.onIntent(intent)
            }
        },
    )
}

@Composable
private fun ActiveSessionScreenContent(
    uiState: ActiveSessionUiState,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        TimerBackground(phase = uiState.phase, isPaused = uiState.isPaused)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            TopBar(onClose = { onIntent(ActiveSessionIntent.Stop) })

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.phaseLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )

                    Spacer(Modifier.height(32.dp))

                    BreathingCircle(
                        phase = uiState.phase,
                        isPaused = uiState.isPaused || uiState.isCompleted,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Crossfade(
                                targetState = uiState.remainingFormatted,
                                animationSpec = tween(300),
                            ) { time ->
                                Text(
                                    text = time,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.ExtraLight,
                                    letterSpacing = (-2).sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            if (!uiState.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { onIntent(ActiveSessionIntent.TogglePause) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = if (uiState.isPaused) "Resume" else "Pause",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ProgressSection(
                currentCycle = uiState.currentCycle,
                totalCycles = uiState.totalCycles,
                isCompleted = uiState.isCompleted,
            )

            Spacer(Modifier.height(24.dp))

            if (!uiState.isCompleted) {
                BottomControls(
                    onSkip = { onIntent(ActiveSessionIntent.Skip) },
                    onStop = { onIntent(ActiveSessionIntent.Stop) },
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimerBackground(phase: SessionPhase, isPaused: Boolean) {
    val overlayAlpha by animateFloatAsState(
        targetValue = when {
            phase == SessionPhase.Break -> 0.92f
            isPaused -> 0.90f
            else -> 0.82f
        },
        animationSpec = tween(1500),
    )

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF0c0e11).copy(alpha = overlayAlpha * 0.7f),
                            0.3f to Color(0xFF0c0e11).copy(alpha = overlayAlpha),
                            1.0f to Color(0xFF0c0e11),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun TopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = "DEEP FOCUS",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )

        IconButton(
            onClick = { /* placeholder */ },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BreathingCircle(
    phase: SessionPhase,
    isPaused: Boolean,
    content: @Composable () -> Unit,
) {
    val breathDuration = when {
        isPaused -> 8000
        phase == SessionPhase.Focus -> 4000
        else -> 6000
    }

    val infiniteTransition = rememberInfiniteTransition()
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = breathDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val currentScale by animateFloatAsState(
        targetValue = if (isPaused) 1f else breathScale,
        animationSpec = tween(1000),
    )

    val ringColor by animateColorAsState(
        targetValue = when (phase) {
            SessionPhase.Focus -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            SessionPhase.Break -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.04f)
        },
        animationSpec = tween(1500),
    )

    val circleBackground by animateColorAsState(
        targetValue = when (phase) {
            SessionPhase.Focus -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.2f)
            SessionPhase.Break -> MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.12f)
        },
        animationSpec = tween(1500),
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(currentScale * 1.08f)
                .border(
                    width = 1.dp,
                    color = ringColor,
                    shape = CircleShape,
                ),
        )

        // Main circle
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(currentScale)
                .clip(CircleShape)
                .background(circleBackground),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun ProgressSection(
    currentCycle: Int,
    totalCycles: Int,
    isCompleted: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (isCompleted) "Session complete" else "Cycle $currentCycle of $totalCycles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(totalCycles) { index ->
                val cycle = index + 1
                val dotColor by animateColorAsState(
                    targetValue = when {
                        cycle < currentCycle || isCompleted -> MaterialTheme.colorScheme.primary
                        cycle == currentCycle -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    animationSpec = tween(500),
                )
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(dotColor),
                )
            }
        }
    }
}

@Composable
private fun BottomControls(
    onSkip: () -> Unit,
    onStop: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.6f))
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onSkip,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onStop() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.StopCircle,
                    contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
