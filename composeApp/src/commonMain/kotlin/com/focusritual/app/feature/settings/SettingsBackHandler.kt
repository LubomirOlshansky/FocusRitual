package com.focusritual.app.feature.settings

import androidx.compose.runtime.Composable

@Composable
internal expect fun SettingsBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
