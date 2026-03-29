package com.focusritual.app.feature.mixer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.component.PlayButton
import com.focusritual.app.core.designsystem.component.SoundTile
import com.focusritual.app.feature.about.AboutSheet
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.background
import org.jetbrains.compose.resources.painterResource

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
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 120.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlayButton(
                            isPlaying = uiState.isPlaying,
                            onClick = { onIntent(MixerIntent.TogglePlayback) },
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = uiState.sceneSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 6.sp,
                        )
                        Spacer(Modifier.height(16.dp))

                        // Start session CTA
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.97f else 1f,
                            animationSpec = tween(150),
                        )

                        val primary = MaterialTheme.colorScheme.primary
                        val primaryContainer = MaterialTheme.colorScheme.primaryContainer

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .fillMaxWidth()
                                .height(48.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .drawBehind {
                                    // Soft ambient glow
                                    drawRoundRect(
                                        brush = Brush.radialGradient(
                                            colorStops = arrayOf(
                                                0.0f to primary.copy(alpha = 0.08f),
                                                0.6f to primary.copy(alpha = 0.02f),
                                                1.0f to Color.Transparent,
                                            ),
                                            center = center,
                                            radius = size.width * 0.55f,
                                        ),
                                        cornerRadius = CornerRadius(999f, 999f),
                                        size = Size(size.width + 16f, size.height + 16f),
                                        topLeft = Offset(-8f, -8f),
                                    )
                                }
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            primary.copy(alpha = 0.35f),
                                            primaryContainer.copy(alpha = 0.20f),
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                                    ),
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                ) {
                                    onStartSession()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "START SESSION",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                            )
                        }
                    }

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

        if (showAboutSheet) {
            AboutSheet(onDismiss = { showAboutSheet = false })
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
