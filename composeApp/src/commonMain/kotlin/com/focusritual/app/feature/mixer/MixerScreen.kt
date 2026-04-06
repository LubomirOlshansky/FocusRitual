package com.focusritual.app.feature.mixer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.component.SoundTile
import com.focusritual.app.feature.about.AboutSheet
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.background
import org.jetbrains.compose.resources.painterResource

private val GlowColor = Color(0xFFB7C8DB)
private val OrganicEasing = CubicBezierEasing(0.3f, 0.0f, 0.15f, 1.0f)

@Composable
fun MixerScreen(
    onStartSession: () -> Unit = {},
    viewModel: MixerViewModel = viewModel { MixerViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MixerScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onStartSession = onStartSession,
    )
}

@Composable
private fun MixerScreenContent(
    uiState: MixerUiState,
    onIntent: (MixerIntent) -> Unit,
    onStartSession: () -> Unit,
) {
    var showAboutSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        ImmersiveBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 120.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HeroSessionButton(
                            isPlaying = uiState.isPlaying,
                            onStartSession = onStartSession,
                        )
                    }

                    // About button — unchanged
                    val aboutInteractionSource = remember { MutableInteractionSource() }
                    val aboutPressed by aboutInteractionSource.collectIsPressedAsState()
                    val aboutScale by animateFloatAsState(
                        targetValue = if (aboutPressed) 0.95f else 1f,
                        animationSpec = tween(200),
                    )
                    val aboutBgAlpha by animateFloatAsState(
                        targetValue = if (aboutPressed) 0.08f else 0f,
                        animationSpec = tween(200),
                    )
                    val aboutIconAlpha by animateFloatAsState(
                        targetValue = if (aboutPressed) 0.40f else 0.60f,
                        animationSpec = tween(200),
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 62.dp, end = 24.dp)
                            .size(34.dp)
                            .graphicsLayer {
                                scaleX = aboutScale
                                scaleY = aboutScale
                            }
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = aboutBgAlpha))
                            .clickable(
                                interactionSource = aboutInteractionSource,
                                indication = null,
                            ) { showAboutSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "About",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = aboutIconAlpha),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            items(
                items = uiState.sounds,
                key = { it.id },
            ) { sound ->
                SoundTile(
                    state = sound,
                    onToggle = { onIntent(MixerIntent.ToggleSound(sound.id)) },
                    onVolumeChange = { volume -> onIntent(MixerIntent.AdjustVolume(sound.id, volume)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                )
            }
        }

        // Floating Current Mix Panel
        CurrentMixPanel(
            uiState = uiState,
            onTogglePlayback = { onIntent(MixerIntent.TogglePlayback) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (showAboutSheet) {
            AboutSheet(onDismiss = { showAboutSheet = false })
        }
    }
}

@Composable
private fun HeroSessionButton(
    isPlaying: Boolean,
    onStartSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()

    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = OrganicEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val glowIntensity by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(800),
    )

    val scaleAnim = 1f + breath * 0.03f * glowIntensity
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.8f else 0.6f,
        animationSpec = tween(300),
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(150),
    )

    val surfaceBright = MaterialTheme.colorScheme.surfaceBright
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .size(150.dp)
            .graphicsLayer {
                scaleX = scaleAnim * pressScale
                scaleY = scaleAnim * pressScale
            }
            .drawBehind {
                // Soft glow aura behind the circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to GlowColor.copy(alpha = 0.15f),
                            0.4f to GlowColor.copy(alpha = 0.06f),
                            1.0f to Color.Transparent,
                        ),
                        radius = size.width * 0.7f,
                    ),
                    alpha = glowIntensity,
                )
            }
            .shadow(24.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        surfaceBright.copy(alpha = bgAlpha),
                        surfaceBright.copy(alpha = bgAlpha * 0.7f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = outlineVariant.copy(alpha = 0.12f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { onStartSession() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "START\nSESSION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            letterSpacing = 2.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun CurrentMixPanel(
    uiState: MixerUiState,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = uiState.activeSoundCount > 0,
        modifier = modifier,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 },
    ) {
        val panelShape = RoundedCornerShape(20.dp)

        Box(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
                .fillMaxWidth()
                .shadow(16.dp, panelShape)
                .clip(panelShape)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.90f),
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                    shape = panelShape,
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CURRENT MIX",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = uiState.activeSoundsSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    )
                }

                IconButton(onClick = onTogglePlayback) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    )
                }

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@Composable
private fun ImmersiveBackground() {
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
                            0.0f to Color.Transparent,
                            0.35f to Color(0xFF0c0e11).copy(alpha = 0.6f),
                            0.55f to Color(0xFF0c0e11).copy(alpha = 0.92f),
                            1.0f to Color(0xFF0c0e11),
                        ),
                    ),
                ),
        )
    }
}
