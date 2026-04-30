package com.focusritual.app.feature.mixer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.feature.mixer.domain.SoundState
import com.focusritual.app.feature.mixer.ui.modal.ActiveSoundRow
import com.focusritual.app.feature.mixer.ui.modal.DoneButton
import com.focusritual.app.feature.mixer.ui.modal.GlobalOrganicMotionRow
import com.focusritual.app.feature.mixer.ui.modal.ModalHeader
import com.focusritual.app.feature.mixer.ui.modal.SaveMixButton

@Composable
fun CurrentMixModal(
    isVisible: Boolean,
    activeSounds: List<SoundState>,
    isPlaying: Boolean,
    anyOrganicOn: Boolean,
    organicMotionSummary: String = "",
    allSoundsOrganic: Boolean = false,
    isDirtyFromPreset: Boolean,
    alreadySaved: Boolean,
    onIntent: (MixerIntent) -> Unit,
    onSaveCurrent: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(activeSounds.isEmpty()) {
        if (activeSounds.isEmpty()) onDismiss()
    }

    val isModalVisible = isVisible && activeSounds.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        ModalScrim(
            isVisible = isModalVisible,
            onDismiss = onDismiss,
        )
        ModalContent(
            isVisible = isModalVisible,
            activeSounds = activeSounds,
            isPlaying = isPlaying,
            isOrganicMotionEnabled = anyOrganicOn,
            organicMotionSummary = organicMotionSummary,
            allSoundsOrganic = allSoundsOrganic,
            isDirtyFromPreset = isDirtyFromPreset,
            alreadySaved = alreadySaved,
            onIntent = onIntent,
            onOpenSaveMix = onSaveCurrent,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ModalScrim(
    isVisible: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 250)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.60f),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onDismiss() },
        )
    }
}

@Composable
private fun ModalContent(
    isVisible: Boolean,
    activeSounds: List<SoundState>,
    isPlaying: Boolean,
    isOrganicMotionEnabled: Boolean,
    organicMotionSummary: String,
    allSoundsOrganic: Boolean,
    isDirtyFromPreset: Boolean,
    alreadySaved: Boolean,
    onIntent: (MixerIntent) -> Unit,
    onOpenSaveMix: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 350, easing = FocusRitualEasing.DeepEaseOut)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 400, easing = FocusRitualEasing.DeepEaseOut),
                initialOffsetY = { fullHeight -> fullHeight },
            ),
        exit = fadeOut(animationSpec = tween(durationMillis = 250, easing = FocusRitualEasing.CinematicIn)) +
            slideOutVertically(
                animationSpec = tween(durationMillis = 300, easing = FocusRitualEasing.CinematicIn),
                targetOffsetY = { fullHeight -> fullHeight },
            ),
    ) {
        val surfaceColor = MaterialTheme.colorScheme.surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ModalHeader(
                    activeSoundCount = activeSounds.size,
                    isPlaying = isPlaying,
                    onDismiss = onDismiss,
                    onTogglePlayback = { onIntent(MixerIntent.TogglePlayback) },
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 0.dp, bottom = 160.dp),
                ) {
                    item(key = "organic-motion") {
                        GlobalOrganicMotionRow(
                            isOrganicMotionEnabled = isOrganicMotionEnabled,
                            organicMotionSummary = organicMotionSummary,
                            allSoundsOrganic = allSoundsOrganic,
                            onToggle = { onIntent(MixerIntent.ToggleGlobalOrganicMotion) },
                        )
                    }

                    items(
                        items = activeSounds,
                        key = { sound -> sound.id },
                    ) { sound ->
                        val onAdjustVolume = remember(sound.id, onIntent) {
                            { volume: Float -> onIntent(MixerIntent.AdjustVolume(soundId = sound.id, volume = volume)) }
                        }
                        val onToggleOrganicMotion = remember(sound.id, onIntent) {
                            { onIntent(MixerIntent.ToggleOrganicMotion(soundId = sound.id)) }
                        }
                        val onRemove = remember(sound.id, onIntent) {
                            { onIntent(MixerIntent.RemoveFromMix(soundId = sound.id)) }
                        }

                        ActiveSoundRow(
                            sound = sound,
                            onAdjustVolume = onAdjustVolume,
                            onToggleOrganicMotion = onToggleOrganicMotion,
                            onRemove = onRemove,
                            modifier = Modifier
                                .animateItem()
                                .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    surfaceColor.copy(alpha = 0f),
                                    surfaceColor.copy(alpha = 0.35f),
                                    surfaceColor.copy(alpha = 0.72f),
                                    surfaceColor,
                                ),
                            ),
                        ),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 7.dp),
                ) {
                    SaveMixButton(
                        isDirty = isDirtyFromPreset,
                        alreadySaved = alreadySaved,
                        onClick = onOpenSaveMix,
                    )
                    DoneButton(onClick = onDismiss)
                }
            }
        }
    }
}
