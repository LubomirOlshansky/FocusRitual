package com.focusritual.app.feature.session

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
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
import com.focusritual.app.core.designsystem.component.SessionModeToggle
import com.focusritual.app.core.designsystem.component.StartSessionButton
import com.focusritual.app.core.designsystem.component.StepperRow
import com.focusritual.app.core.protectfocus.ProtectFocusConfig
import com.focusritual.app.core.protectfocus.ProtectFocusController
import com.focusritual.app.core.protectfocus.ProtectFocusState
import com.focusritual.app.core.protectfocus.SetupResult
import kotlinx.coroutines.launch

@Composable
fun FocusSessionScreen(
    onClose: () -> Unit,
    onStartSession: (SessionConfig) -> Unit,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when (mode) {
                        SessionMode.Focus -> "Focus Session".uppercase()
                        SessionMode.Sleep -> "Sleep Session".uppercase()
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.18.em,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                )
                CloseButton(onClick = { onIntent(FocusSessionIntent.Close) })
            }

            Spacer(Modifier.height(22.dp))
            SessionModeToggle(selectedMode = mode, onModeChange = onModeChange)
            Spacer(Modifier.height(22.dp))

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
            StartSessionButton(onClick = { onIntent(FocusSessionIntent.StartSession) })
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
    uiState.presets.forEach { preset ->
        val isSelected = uiState.selectedPresetId == preset.id
        PresetRow(
            label = preset.label,
            isSelected = isSelected,
            onClick = { onIntent(FocusSessionIntent.SelectPreset(preset.id)) },
        )
    }

    PresetRow(
        label = "Custom",
        isSelected = uiState.isCustomSelected,
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
                label = "Focus",
                value = uiState.customFocusMinutes,
                unit = "min",
                onDecrement = { onIntent(FocusSessionIntent.AdjustFocus(-1)) },
                onIncrement = { onIntent(FocusSessionIntent.AdjustFocus(1)) },
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.09f),
            )
            StepperRow(
                label = "Break",
                value = uiState.customBreakMinutes,
                unit = "min",
                onDecrement = { onIntent(FocusSessionIntent.AdjustBreak(-1)) },
                onIncrement = { onIntent(FocusSessionIntent.AdjustBreak(1)) },
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.09f),
            )
            StepperRow(
                label = "Cycles",
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
                text = "Protect Focus",
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
            Text(
                text = "Block distracting apps during session",
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
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
            label = "Duration",
            value = uiState.sleepDurationMinutes,
            unit = "min",
            onDecrement = { onIntent(FocusSessionIntent.AdjustSleepDuration(-1)) },
            onIncrement = { onIntent(FocusSessionIntent.AdjustSleepDuration(1)) },
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.09f),
        )
        StepperRow(
            label = "Fade out",
            value = uiState.sleepFadeOutMinutes,
            unit = "min",
            onDecrement = { onIntent(FocusSessionIntent.AdjustSleepFadeOut(-1)) },
            onIncrement = { onIntent(FocusSessionIntent.AdjustSleepFadeOut(1)) },
        )
        Text(
            text = "sounds fade over the last portion",
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.04.em,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f),
            modifier = Modifier.padding(start = 13.dp, top = 2.dp, bottom = 12.dp),
        )
    }
}

@Composable
private fun PresetRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(17.dp)
                .border(
                    width = 0.5.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f),
                            shape = CircleShape,
                        ),
                )
            }
        }
        val labelColor by animateColorAsState(
            targetValue = if (isSelected) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f)
            },
            animationSpec = tween(250),
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.01).em,
            color = labelColor,
        )
    }
}
