package com.focusritual.app.core.designsystem.component

import androidx.compose.runtime.Composable

@Composable
expect fun ProtectFocusSetupSheet(
    isSettingUp: Boolean = false,
    onDismiss: () -> Unit,
    onChooseBlockedApps: () -> Unit,
)
