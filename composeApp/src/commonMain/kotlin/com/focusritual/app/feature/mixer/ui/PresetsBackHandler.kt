package com.focusritual.app.feature.mixer.ui

import androidx.compose.runtime.Composable

@Composable
internal expect fun PresetsBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
