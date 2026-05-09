package com.focusritual.app.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusritual.app.core.designsystem.component.VolumeSlider
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.core.designsystem.theme.Spacing
import com.focusritual.app.feature.settings.SettingsIntent
import com.focusritual.app.feature.settings.SettingsUiState
import focusritual.composeapp.generated.resources.Res
import focusritual.composeapp.generated.resources.settings_app_haptics_label
import focusritual.composeapp.generated.resources.settings_app_haptics_subtitle
import focusritual.composeapp.generated.resources.settings_app_rate
import focusritual.composeapp.generated.resources.settings_app_share
import focusritual.composeapp.generated.resources.settings_app_system_language
import focusritual.composeapp.generated.resources.settings_legal_privacy
import focusritual.composeapp.generated.resources.settings_legal_terms
import focusritual.composeapp.generated.resources.settings_preferred_language
import focusritual.composeapp.generated.resources.settings_section_app
import focusritual.composeapp.generated.resources.settings_section_audio
import focusritual.composeapp.generated.resources.settings_section_legal
import focusritual.composeapp.generated.resources.settings_section_support
import focusritual.composeapp.generated.resources.settings_support_contact
import focusritual.composeapp.generated.resources.settings_support_sound_credits
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsHome(
    uiState: SettingsUiState,
    currentLanguageName: String = "",
    onIntent: (SettingsIntent) -> Unit,
    listState: LazyListState,
) {
    val bottomInset = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 10.dp,
            end = 20.dp,
            bottom = 20.dp + bottomInset,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SettingsSection {
                SectionLabel(stringResource(Res.string.settings_section_audio))
                AudioGroup(uiState = uiState, onIntent = onIntent)
            }
        }
        item {
            SettingsSection {
                SectionLabel(stringResource(Res.string.settings_section_app))
                AppGroup(
                    uiState = uiState,
                    currentLanguageName = currentLanguageName,
                    onIntent = onIntent,
                )
            }
        }
        item {
            SettingsSection {
                SectionLabel(stringResource(Res.string.settings_section_support))
                SupportGroup(
                    onContact = { onIntent(SettingsIntent.ContactSupport) },
                    onShowSoundCredits = { onIntent(SettingsIntent.OpenSoundCredits) },
                )
            }
        }
        item {
            SettingsSection {
                SectionLabel(stringResource(Res.string.settings_section_legal))
                LegalGroup(
                    onPrivacy = { onIntent(SettingsIntent.OpenPrivacyPolicy) },
                    onTerms = { onIntent(SettingsIntent.OpenTermsOfUse) },
                )
            }
        }
    }
}

@Composable
private fun AudioGroup(
    uiState: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
) {
    SettingsGroup {
        AudioSettingsRow(
            icon = Icons.Default.VolumeUp,
            label = "Use while media is playing",
            subtitle = "Continues when Spotify, podcasts or calls are active",
            showChevron = false,
        ) {
            FocusSwitch(uiState.mixWithOthersEnabled) { enabled ->
                onIntent(SettingsIntent.SetMixWithOthers(enabled))
            }
        }
        Column(
            modifier = Modifier.alpha(if (uiState.mixWithOthersEnabled) 1f else 0.35f),
        ) {
            AudioSettingsRow(
                icon = Icons.Default.Tune,
                label = "Quiet down for other audio",
                subtitle = "Lowers volume when another app starts playing — restores after",
                showChevron = false,
                enabled = uiState.mixWithOthersEnabled,
            ) {
                FocusSwitch(
                    checked = uiState.duckOthersEnabled,
                    enabled = uiState.mixWithOthersEnabled,
                ) { enabled ->
                    onIntent(SettingsIntent.SetDuckOthers(enabled))
                }
            }
            AnimatedVisibility(
                visible = uiState.mixWithOthersEnabled && uiState.duckOthersEnabled,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 220, easing = FocusRitualEasing.DeepEaseOut),
                ) + fadeIn(tween(durationMillis = 180, easing = FocusRitualEasing.Atmospheric)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 180, easing = FocusRitualEasing.CinematicIn),
                ) + fadeOut(tween(durationMillis = 140, easing = FocusRitualEasing.CinematicIn)),
            ) {
                DuckLevelControl(
                    value = uiState.duckLevel,
                    onValueChange = { level -> onIntent(SettingsIntent.SetDuckLevel(level)) },
                )
            }
        }
    }
}

@Composable
private fun AudioSettingsRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    showChevron: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.985f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FocusRitualEasing.DeepEaseOut),
    )
    val pressedAlpha by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.05f else 0f,
        animationSpec = tween(durationMillis = 110, easing = FocusRitualEasing.DeepEaseOut),
    )
    val rowModifier = if (onClick != null && enabled) {
        Modifier.clickable(interactionSource = interactionSource, indication = null) { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .then(rowModifier)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = pressedAlpha))
            .padding(start = 14.dp, top = 13.dp, end = 14.dp, bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AudioIconCircle(icon)
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
private fun AudioIconCircle(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.70f),
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
private fun DuckLevelControl(value: Float, onValueChange: (Float) -> Unit) {
    val percent = (value.coerceIn(0.10f, 0.70f) * 100).roundToInt()
    
    // Map actual duck level (0.10..0.70) to normalized slider value (0..1)
    val normalizedValue = ((value.coerceIn(0.10f, 0.70f) - 0.10f) / 0.60f).coerceIn(0f, 1f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, top = Spacing.xs, end = Spacing.lg, bottom = Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Volume when interrupted",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.54f),
                modifier = Modifier.weight(1f),
            )
            Text(
                "$percent%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        VolumeSlider(
            value = normalizedValue,
            onValueChange = { normalized ->
                // Map normalized (0..1) back to actual duck level (0.10..0.70)
                val duckLevel = 0.10f + normalized * 0.60f
                onValueChange(duckLevel)
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AppGroup(
    uiState: SettingsUiState,
    currentLanguageName: String = "",
    onIntent: (SettingsIntent) -> Unit,
) {
    val systemLabel = if (currentLanguageName.isNotEmpty()) {
        "${stringResource(Res.string.settings_app_system_language)}: $currentLanguageName"
    } else {
        stringResource(Res.string.settings_app_system_language)
    }
    SettingsGroup {
        SettingsRow(
            icon = Icons.Outlined.Language,
            label = stringResource(Res.string.settings_preferred_language),
            trailingValue = systemLabel,
            onClick = { onIntent(SettingsIntent.OpenLanguageSettings) },
        )
        SettingsRow(
            icon = Icons.Outlined.Vibration,
            label = stringResource(Res.string.settings_app_haptics_label),
            subtitle = stringResource(Res.string.settings_app_haptics_subtitle),
            showChevron = false,
            trailing = {
                FocusSwitch(uiState.hapticsEnabled) { enabled ->
                    onIntent(SettingsIntent.SetHapticsEnabled(enabled))
                }
            },
        )
        SettingsRow(
            icon = Icons.Outlined.StarBorder,
            label = stringResource(Res.string.settings_app_rate),
            onClick = { onIntent(SettingsIntent.RateApp) },
        )
        SettingsRow(
            icon = Icons.Outlined.Share,
            label = stringResource(Res.string.settings_app_share),
            onClick = { onIntent(SettingsIntent.ShareApp) },
        )
    }
}

@Composable
private fun SupportGroup(
    onContact: () -> Unit,
    onShowSoundCredits: () -> Unit,
) {
    SettingsGroup {
        SettingsRow(
            icon = Icons.Outlined.Email,
            label = stringResource(Res.string.settings_support_contact),
            subtitle = "hello@focusritual.app",
            onClick = onContact,
        )
        SettingsRow(
            icon = Icons.Outlined.GraphicEq,
            label = stringResource(Res.string.settings_support_sound_credits),
            onClick = onShowSoundCredits,
        )
    }
}

@Composable
private fun LegalGroup(
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    SettingsGroup {
        SettingsRow(Icons.Outlined.Policy, stringResource(Res.string.settings_legal_privacy), onClick = onPrivacy)
        SettingsRow(Icons.Outlined.Gavel, stringResource(Res.string.settings_legal_terms), onClick = onTerms)
    }
}
