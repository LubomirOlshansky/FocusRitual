package com.focusritual.app.core.designsystem.component

import androidx.compose.runtime.Composable

@Composable
expect fun ProtectFocusSetupSheet(
    isSettingUp: Boolean,
    onDismiss: () -> Unit,
    onChooseBlockedApps: () -> Unit,
)
