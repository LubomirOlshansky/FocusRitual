package com.focusritual.app.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ProtectFocusCard(
    isConfigured: Boolean = false,
    blockedAppCount: Int = 0,
    isEnabled: Boolean = true,
    onToggle: (Boolean) -> Unit = {},
    onEditBlockedApps: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
