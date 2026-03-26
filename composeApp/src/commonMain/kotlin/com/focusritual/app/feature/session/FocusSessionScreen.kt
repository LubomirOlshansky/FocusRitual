package com.focusritual.app.feature.session

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FocusSessionScreen(
    onClose: () -> Unit,
    onStartSession: (SessionConfig) -> Unit,
    viewModel: FocusSessionViewModel = viewModel { FocusSessionViewModel() },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FocusSessionScreenContent(
        uiState = uiState,
        onIntent = { intent ->
            when (intent) {
                FocusSessionIntent.Close -> onClose()
                FocusSessionIntent.StartSession -> onStartSession(viewModel.resolveConfig())
                else -> viewModel.onIntent(intent)
            }
        },
    )
}

@Composable
private fun FocusSessionScreenContent(
    uiState: FocusSessionUiState,
    onIntent: (FocusSessionIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "FOCUS SESSION",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
            IconButton(
                onClick = { onIntent(FocusSessionIntent.Close) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
        ) {
            // Preset radio buttons
            uiState.presets.forEach { preset ->
                val isSelected = uiState.selectedPresetId == preset.id
                PresetRow(
                    label = preset.label,
                    isSelected = isSelected,
                    onClick = { onIntent(FocusSessionIntent.SelectPreset(preset.id)) },
                )
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(4.dp))

            // Custom expandable card
            CustomCard(
                isSelected = uiState.isCustomSelected,
                focusMinutes = uiState.customFocusMinutes,
                breakMinutes = uiState.customBreakMinutes,
                sessions = uiState.customSessions,
                onSelect = { onIntent(FocusSessionIntent.SelectCustom) },
                onIntent = onIntent,
            )
        }

        // Footer CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onIntent(FocusSessionIntent.StartSession) }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "START SESSION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        }
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioCircle(isSelected = isSelected)
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun RadioCircle(isSelected: Boolean) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(300),
    )
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}

@Composable
private fun CustomCard(
    isSelected: Boolean,
    focusMinutes: Int,
    breakMinutes: Int,
    sessions: Int,
    onSelect: () -> Unit,
    onIntent: (FocusSessionIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp),
                        )
                } else {
                    Modifier
                },
            ),
    ) {
        // Custom header row (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onSelect() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioCircle(isSelected = isSelected)
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Custom",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        // Expanded stepper section (only when selected)
        if (isSelected) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepperRow(
                    label = "FOCUS",
                    value = focusMinutes,
                    unit = "MIN",
                    onDecrement = { onIntent(FocusSessionIntent.AdjustFocus(-1)) },
                    onIncrement = { onIntent(FocusSessionIntent.AdjustFocus(1)) },
                )
                StepperRow(
                    label = "BREAK",
                    value = breakMinutes,
                    unit = "MIN",
                    onDecrement = { onIntent(FocusSessionIntent.AdjustBreak(-1)) },
                    onIncrement = { onIntent(FocusSessionIntent.AdjustBreak(1)) },
                )
                StepperRow(
                    label = "SESSIONS",
                    value = sessions,
                    unit = "QTY",
                    onDecrement = { onIntent(FocusSessionIntent.AdjustSessions(-1)) },
                    onIncrement = { onIntent(FocusSessionIntent.AdjustSessions(1)) },
                )
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    unit: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.outline,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease $label",
                    tint = MaterialTheme.colorScheme.outline,
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.widthIn(min = 48.dp),
            ) {
                Text(
                    text = "$value",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase $label",
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
