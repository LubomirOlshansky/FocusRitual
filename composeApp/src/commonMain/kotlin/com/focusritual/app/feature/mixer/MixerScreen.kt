package com.focusritual.app.feature.mixer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.feature.mixer.ui.SoundTile
import com.focusritual.app.feature.about.AboutSheet
import com.focusritual.app.feature.mixer.domain.CurrentMixSummary
import com.focusritual.app.feature.mixer.domain.SoundCategory
import com.focusritual.app.feature.mixer.domain.SoundState
import com.focusritual.app.feature.mixer.ui.CategoryPillRow
import com.focusritual.app.feature.mixer.ui.CurrentMixPanel
import com.focusritual.app.feature.mixer.ui.HeroSessionButton
import com.focusritual.app.feature.mixer.ui.ImmersiveBackground
import com.focusritual.app.feature.mixer.ui.SectionHeader

@Composable
fun MixerScreen(
    onStartSession: () -> Unit = {},
    viewModel: MixerViewModel = viewModel { MixerViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredSounds by viewModel.filteredSounds.collectAsStateWithLifecycle()
    MixerScreenContent(
        uiState = uiState,
        filteredSounds = filteredSounds,
        onIntent = viewModel::onIntent,
        onStartSession = onStartSession,
    )
}

@Composable
private fun MixerScreenContent(
    uiState: MixerUiState,
    filteredSounds: Map<SoundCategory, List<SoundState>>,
    onIntent: (MixerIntent) -> Unit,
    onStartSession: () -> Unit,
) {
    var showAboutSheet by remember { mutableStateOf(false) }
    var showCurrentMixModal by remember { mutableStateOf(false) }

    val mixSummary = remember(uiState.isPlaying, uiState.activeSoundCount, uiState.activeSoundsSummary) {
        CurrentMixSummary(
            isPlaying = uiState.isPlaying,
            activeSoundCount = uiState.activeSoundCount,
            activeSoundsSummary = uiState.activeSoundsSummary,
        )
    }
    val activeSounds = remember(uiState.sounds) { uiState.sounds.filter { it.isEnabled } }
    val anyOrganicOn = remember(activeSounds) { activeSounds.any { it.organicMotion } }

    Box(Modifier.fillMaxSize()) {
        ImmersiveBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp),
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
            // Category pill row
            item {
                CategoryPillRow(
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = { onIntent(MixerIntent.SelectCategory(it)) },
                )
            }

            // Grouped sound sections
            filteredSounds.forEach { (category, sounds) ->
                item(key = "header_${category.name}") {
                    SectionHeader(
                        category = category,
                        activeCount = sounds.count { it.isEnabled },
                    )
                }
                items(
                    items = sounds,
                    key = { it.id },
                ) { sound ->
                    val onToggle = remember(sound.id, onIntent) {
                        { _: Boolean -> onIntent(MixerIntent.ToggleSound(sound.id)) }
                    }
                    val onVolumeChange = remember(sound.id, onIntent) {
                        { volume: Float -> onIntent(MixerIntent.AdjustVolume(sound.id, volume)) }
                    }
                    val onToggleOrganic = remember(sound.id, onIntent) {
                        { onIntent(MixerIntent.ToggleOrganicMotion(sound.id)) }
                    }
                    SoundTile(
                        state = sound,
                        onToggle = onToggle,
                        onVolumeChange = onVolumeChange,
                        onToggleOrganicMotion = onToggleOrganic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 7.dp),
                    )
                }
            }
        }

        // Floating Current Mix Panel
        CurrentMixPanel(
            summary = mixSummary,
            onTogglePlayback = { onIntent(MixerIntent.TogglePlayback) },
            onPanelTap = { showCurrentMixModal = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (showAboutSheet) {
            AboutSheet(onDismiss = { showAboutSheet = false })
        }

        CurrentMixModal(
            isVisible = showCurrentMixModal,
            activeSounds = activeSounds,
            isPlaying = uiState.isPlaying,
            anyOrganicOn = anyOrganicOn,
            onIntent = onIntent,
            onDismiss = { showCurrentMixModal = false },
        )
    }
}
