package com.focusritual.app.feature.mixer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.focusritual.app.feature.mixer.domain.SoundState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusritual.app.feature.mixer.ui.modal.ActiveSoundRow
import com.focusritual.app.feature.mixer.ui.modal.DoneButton
import com.focusritual.app.feature.mixer.ui.modal.GlobalOrganicMotionRow
import com.focusritual.app.feature.mixer.ui.modal.ModalHeader

@Composable
fun CurrentMixModal(
    isVisible: Boolean,
    activeSounds: List<SoundState>,
    isPlaying: Boolean,
    anyOrganicOn: Boolean,
    onIntent: (MixerIntent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(activeSounds.isEmpty()) {
        if (activeSounds.isEmpty()) onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible && activeSounds.isNotEmpty(),
        enter = fadeIn(tween(350)) + slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it },
        exit = fadeOut(tween(250)) + slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
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
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                ) {
                    item {
                        GlobalOrganicMotionRow(
                            checked = anyOrganicOn,
                            onToggle = { onIntent(MixerIntent.ToggleGlobalOrganicMotion) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(
                        items = activeSounds,
                        key = { it.id },
                    ) { sound ->
                        val onAdjustVolume = remember(sound.id, onIntent) {
                            { v: Float -> onIntent(MixerIntent.AdjustVolume(sound.id, v)) }
                        }
                        val onToggleOrganic = remember(sound.id, onIntent) {
                            { onIntent(MixerIntent.ToggleOrganicMotion(sound.id)) }
                        }
                        val onRemove = remember(sound.id, onIntent) {
                            { onIntent(MixerIntent.RemoveFromMix(sound.id)) }
                        }
                        ActiveSoundRow(
                            sound = sound,
                            onAdjustVolume = onAdjustVolume,
                            onToggleOrganicMotion = onToggleOrganic,
                            onRemove = onRemove,
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 24.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            DoneButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
            )
        }
    }
}
