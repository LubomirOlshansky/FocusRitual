package com.focusritual.app.feature.session

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.component.CloseButton
import com.focusritual.app.core.designsystem.component.ProtectFocusSetupSheet
import com.focusritual.app.core.designsystem.component.RitualRadioRow
import com.focusritual.app.feature.session.ui.SessionModeToggle
import com.focusritual.app.feature.session.ui.SessionTypeHeaderCard
import com.focusritual.app.core.designsystem.component.StartSessionButton
import com.focusritual.app.core.designsystem.component.StepperRow
import com.focusritual.app.core.haptic.HapticController
import com.focusritual.app.core.protectfocus.ProtectFocusConfig
import com.focusritual.app.core.protectfocus.ProtectFocusController
import com.focusritual.app.core.protectfocus.ProtectFocusState
import com.focusritual.app.core.protectfocus.SetupResult
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.session_break
import focusritual.composeapp.generated.resources.session_custom
import focusritual.composeapp.generated.resources.session_cycles
import focusritual.composeapp.generated.resources.session_duration
import focusritual.composeapp.generated.resources.session_fade_out
import focusritual.composeapp.generated.resources.session_focus
import focusritual.composeapp.generated.resources.session_min_unit
import focusritual.composeapp.generated.resources.session_preset_long
import focusritual.composeapp.generated.resources.session_preset_medium
import focusritual.composeapp.generated.resources.session_preset_short
import focusritual.composeapp.generated.resources.session_protect_focus_subtitle
import focusritual.composeapp.generated.resources.session_protect_focus_title
import focusritual.composeapp.generated.resources.session_sleep_hint
import focusritual.composeapp.generated.resources.start_focus_session
import focusritual.composeapp.generated.resources.start_sleep_session
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun FocusSessionScreen(
    onClose: () -> Unit,
    onStartSession: (SessionConfig) -> Unit,
    hapticController: HapticController = HapticController(),
    viewModel: FocusSessionViewModel = viewModel { FocusSessionViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mode by remember { mutableStateOf(SessionMode.Focus) }

    FocusSessionScreenContent(
        uiState = uiState,
        mode = mode,
        onModeChange = { mode = it },
        onIntent = { intent ->
            when (intent) {
                FocusSessionIntent.Close -> onClose()
                FocusSessionIntent.StartSession -> {
                    val config = when (mode) {
                        SessionMode.Focus -> viewModel.resolveConfig()
                        SessionMode.Sleep -> SessionConfig(
                            mode = SessionMode.Sleep,
                            focusMinutes = 0,
                            breakMinutes = 0,
                            totalCycles = 1,
                            sleepDurationMinutes = uiState.sleepDurationMinutes,
                            sleepFadeOutMinutes = uiState.sleepFadeOutMinutes,
                        )
                    }
                    hapticController.sessionStarted()
                    onStartSession(config)
                }
                else -> viewModel.onIntent(intent)
            }
        },
    )
}

@Composable
private fun FocusSessionScreenContent(
    uiState: FocusSessionUiState,
    mode: SessionMode,
    onModeChange: (SessionMode) -> Unit,
    onIntent: (FocusSessionIntent) -> Unit,
) {
    var protectFocusState by remember { mutableStateOf<ProtectFocusState>(ProtectFocusState.Idle) }
    var protectFocusConfig by remember { mutableStateOf(ProtectFocusConfig()) }
    val protectFocusController = remember { ProtectFocusController() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CloseButton(onClick = { onIntent(FocusSessionIntent.Close) })
            }

            Spacer(Modifier.height(12.dp))
            SessionTypeHeaderCard(mode = mode)
            Spacer(Modifier.height(14.dp))
            SessionModeToggle(selectedMode = mode, onModeChange = onModeChange)
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                Crossfade(
                    targetState = mode,
                    animationSpec = tween(350),
                ) { currentMode ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (currentMode) {
                            SessionMode.Focus -> FocusModeContent(
                                uiState = uiState,
                                onIntent = onIntent,
                                onProtectFocusClick = {
                                    protectFocusState = ProtectFocusState.SheetOpen
                                },
                            )
                            SessionMode.Sleep -> SleepModeContent(
                                uiState = uiState,
                                onIntent = onIntent,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            StartSessionButton(
                onClick = { onIntent(FocusSessionIntent.StartSession) },
                label = stringResource(
                    if (mode == SessionMode.Focus) Res.string.start_focus_session
                    else Res.string.start_sleep_session,
                ),
            )
            Spacer(Modifier.height(24.dp))
        }

        val showSheet = protectFocusState is ProtectFocusState.SheetOpen ||
            protectFocusState is ProtectFocusState.SettingUp ||
            protectFocusState is ProtectFocusState.PermissionDenied

        if (showSheet) {
            ProtectFocusSetupSheet(
                isSettingUp = protectFocusState is ProtectFocusState.SettingUp,
                onDismiss = { protectFocusState = ProtectFocusState.Idle },
                onChooseBlockedApps = {
                    protectFocusState = ProtectFocusState.SettingUp
                    scope.launch {
                        val result = protectFocusController.requestSetup()
                        protectFocusState = when (result) {
                            SetupResult.Success -> {
                                protectFocusConfig = ProtectFocusConfig(
                                    isConfigured = true,
                                    blockedAppCount = 12,
                                    isEnabled = true,
                                )
                                ProtectFocusState.Idle
                            }
                            SetupResult.Cancelled -> ProtectFocusState.Idle
                            SetupResult.PermissionDenied -> ProtectFocusState.PermissionDenied
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun FocusModeContent(
    uiState: FocusSessionUiState,
    onIntent: (FocusSessionIntent) -> Unit,
    onProtectFocusClick: () -> Unit,
) {
    uiState.presets.forEachIndexed { index, preset ->
        if (index > 0) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            )
        }
        val isSelected = uiState.selectedPresetId == preset.id
        RitualRadioRow(
            label = preset.localizedLabel(),
            selected = isSelected,
            onClick = { onIntent(FocusSessionIntent.SelectPreset(preset.id)) },
        )
    }

    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    )
    RitualRadioRow(
        label = stringResource(Res.string.session_custom),
        selected = uiState.isCustomSelected,
        onClick = { onIntent(FocusSessionIntent.SelectCustom) },
    )

    if (uiState.isCustomSelected) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp),
                ),
        ) {
            StepperRow(
                label = stringResource(Res.string.session_focus),
                value = uiState.customFocusMinutes,
                unit = stringResource(Res.string.session_min_unit),
                onDecrement = { onIntent(FocusSessionIntent.AdjustFocus(-1)) },
                onIncrement = { onIntent(FocusSessionIntent.AdjustFocus(1)) },
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.09f),
            )
            StepperRow(
                label = stringResource(Res.string.session_break),
                value = uiState.customBreakMinutes,
                unit = stringResource(Res.string.session_min_unit),
                onDecrement = { onIntent(FocusSessionIntent.AdjustBreak(-1)) },
                onIncrement = { onIntent(FocusSessionIntent.AdjustBreak(1)) },
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.09f),
            )
            StepperRow(
                label = stringResource(Res.string.session_cycles),
                value = uiState.customSessions,
                unit = "×",
                onDecrement = { onIntent(FocusSessionIntent.AdjustSessions(-1)) },
                onIncrement = { onIntent(FocusSessionIntent.AdjustSessions(1)) },
            )
        }
    }

    Spacer(Modifier.height(22.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.60f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onProtectFocusClick() }
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
                modifier = Modifier.size(15.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(Res.string.session_protect_focus_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
            Text(
                text = stringResource(Res.string.session_protect_focus_subtitle),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
        }
        Text(
            text = "›",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
        )
    }
}

@Composable
private fun SleepModeContent(
    uiState: FocusSessionUiState,
    onIntent: (FocusSessionIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
            ),
    ) {
        StepperRow(
            label = stringResource(Res.string.session_duration),
            value = uiState.sleepDurationMinutes,
            unit = stringResource(Res.string.session_min_unit),
            onDecrement = { onIntent(FocusSessionIntent.AdjustSleepDuration(-1)) },
            onIncrement = { onIntent(FocusSessionIntent.AdjustSleepDuration(1)) },
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.09f),
        )
        StepperRow(
            label = stringResource(Res.string.session_fade_out),
            value = uiState.sleepFadeOutMinutes,
            unit = stringResource(Res.string.session_min_unit),
            onDecrement = { onIntent(FocusSessionIntent.AdjustSleepFadeOut(-1)) },
            onIncrement = { onIntent(FocusSessionIntent.AdjustSleepFadeOut(1)) },
        )
        Text(
            text = stringResource(Res.string.session_sleep_hint),
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.04.em,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
            modifier = Modifier.padding(start = 13.dp, top = 2.dp, bottom = 12.dp),
        )
    }
}

@Composable
private fun SessionPreset.localizedLabel(): String = when (id) {
    "short" -> stringResource(Res.string.session_preset_short)
    "medium" -> stringResource(Res.string.session_preset_medium)
    "long" -> stringResource(Res.string.session_preset_long)
    else -> label
}
