package com.focusritual.app.feature.mixer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Settings
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
import com.focusritual.app.feature.mixer.domain.CurrentMixSummary
import com.focusritual.app.feature.mixer.domain.SoundCategory
import com.focusritual.app.feature.mixer.domain.SoundState
import com.focusritual.app.feature.mixer.ui.CategoryPillRow
import com.focusritual.app.feature.mixer.ui.SavedMixesGhostRow
import com.focusritual.app.feature.mixer.ui.CurrentMixPanel
import com.focusritual.app.feature.mixer.ui.HeroSessionButton
import com.focusritual.app.feature.mixer.ui.ImmersiveBackground
import com.focusritual.app.feature.mixer.ui.PresetsSheet
import com.focusritual.app.feature.mixer.ui.SectionHeader
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.mixer_organic_all_sounds
import focusritual.composeapp.generated.resources.mixer_organic_off
import focusritual.composeapp.generated.resources.mixer_organic_only
import focusritual.composeapp.generated.resources.mixer_organic_sounds_count
import focusritual.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun MixerScreen(
    onStartSession: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: MixerViewModel = viewModel { MixerViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredSounds by viewModel.filteredSounds.collectAsStateWithLifecycle()
    MixerScreenContent(
        uiState = uiState,
        filteredSounds = filteredSounds,
        onIntent = viewModel::onIntent,
        onStartSession = onStartSession,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
private fun MixerScreenContent(
    uiState: MixerUiState,
    filteredSounds: Map<SoundCategory, List<SoundState>>,
    onIntent: (MixerIntent) -> Unit,
    onStartSession: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var showCurrentMixModal by remember { mutableStateOf(false) }
    var showPresetsSheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveDialogState by remember { mutableStateOf(SaveDialogState.Input) }

    val openSaveDialog: () -> Unit = {
        saveDialogState = SaveDialogState.Input
        showSaveDialog = true
    }

    val mixSummary = remember(uiState.isPlaying, uiState.activeSoundCount, uiState.activeSoundsSummary) {
        CurrentMixSummary(
            isPlaying = uiState.isPlaying,
            activeSoundCount = uiState.activeSoundCount,
            activeSoundsSummary = uiState.activeSoundsSummary,
        )
    }
    val activeSounds = remember(uiState.sounds) { uiState.sounds.filter { it.isEnabled } }
    val soundNamesById = remember(uiState.sounds) { uiState.sounds.associate { it.id to it.name } }
    val anyOrganicOn = remember(activeSounds) { activeSounds.any { it.organicMotion } }
    val allSoundsOrganic = remember(activeSounds) {
        activeSounds.isNotEmpty() && activeSounds.all { sound -> sound.organicMotion }
    }
    val activeOrganicMotionSummary = organicMotionSummary(activeSounds = activeSounds)
    val alreadySaved = remember(uiState.loadedPresetId, uiState.isDirtyFromPreset) {
        uiState.loadedPresetId != null && !uiState.isDirtyFromPreset
    }
    val existingMixNames = remember(uiState.savedMixes) {
        uiState.savedMixes.map { it.name.trim().lowercase() }.toSet()
    }

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

                    val settingsInteractionSource = remember { MutableInteractionSource() }
                    val settingsPressed by settingsInteractionSource.collectIsPressedAsState()
                    val settingsScale by animateFloatAsState(
                        targetValue = if (settingsPressed) 0.97f else 1f,
                        animationSpec = tween(160),
                    )
                    val settingsIconAlpha by animateFloatAsState(
                        targetValue = if (settingsPressed) 0.44f else 0.62f,
                        animationSpec = tween(160),
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 62.dp, end = 24.dp)
                            .size(28.dp)
                            .graphicsLayer {
                                scaleX = settingsScale
                                scaleY = settingsScale
                            }
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.70f))
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                                shape = CircleShape,
                            )
                            .clickable(
                                interactionSource = settingsInteractionSource,
                                indication = null,
                            ) { onOpenSettings() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(Res.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = settingsIconAlpha),
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }
            item {
                CategoryPillRow(
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = { onIntent(MixerIntent.SelectCategory(it)) },
                )
            }
            if (uiState.savedMixes.isNotEmpty()) {
                item(key = "saved_mixes_ghost") {
                    SavedMixesGhostRow(
                        count = uiState.savedMixes.size,
                        onTap = { showPresetsSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .animateItem(fadeInSpec = tween(300)),
                    )
                }
            }

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

        CurrentMixPanel(
            summary = mixSummary,
            onTogglePlayback = { onIntent(MixerIntent.TogglePlayback) },
            onPanelTap = { showCurrentMixModal = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        CurrentMixModal(
            isVisible = showCurrentMixModal,
            activeSounds = activeSounds,
            isPlaying = uiState.isPlaying,
            anyOrganicOn = anyOrganicOn,
            organicMotionSummary = activeOrganicMotionSummary,
            allSoundsOrganic = allSoundsOrganic,
            isDirtyFromPreset = uiState.isDirtyFromPreset,
            alreadySaved = alreadySaved,
            onIntent = onIntent,
            onSaveCurrent = openSaveDialog,
            onDismiss = { showCurrentMixModal = false },
        )

        PresetsSheet(
            isVisible = showPresetsSheet,
            presets = uiState.savedMixes,
            loadedPresetId = uiState.loadedPresetId,
            isDirtyFromPreset = uiState.isDirtyFromPreset,
            soundNamesById = soundNamesById,
            onLoad = { presetId ->
                onIntent(MixerIntent.LoadMix(presetId))
            },
            onSaveCurrent = {
                openSaveDialog()
            },
            onDelete = { presetId -> onIntent(MixerIntent.DeleteMix(presetId)) },
            onDismiss = { showPresetsSheet = false },
        )

        if (showSaveDialog) {
            SaveMixDialog(
                activeSounds = activeSounds,
                dialogState = saveDialogState,
                existingMixNames = existingMixNames,
                onSave = { name ->
                    saveDialogState = SaveDialogState.Saved
                    onIntent(MixerIntent.SaveCurrentMix(name))
                },
                onDone = {
                    showSaveDialog = false
                    saveDialogState = SaveDialogState.Input
                },
                onDismiss = {
                    showSaveDialog = false
                    saveDialogState = SaveDialogState.Input
                },
            )
        }
    }
}

@Composable
internal fun organicMotionSummary(activeSounds: List<SoundState>): String {
    val organicSounds = activeSounds.filter { sound -> sound.organicMotion }
    return when {
        organicSounds.isEmpty() -> stringResource(Res.string.mixer_organic_off)
        organicSounds.size == activeSounds.size -> stringResource(Res.string.mixer_organic_all_sounds)
        organicSounds.size == 1 -> stringResource(Res.string.mixer_organic_only, organicSounds.first().name)
        else -> stringResource(Res.string.mixer_organic_sounds_count, organicSounds.size)
    }
}
