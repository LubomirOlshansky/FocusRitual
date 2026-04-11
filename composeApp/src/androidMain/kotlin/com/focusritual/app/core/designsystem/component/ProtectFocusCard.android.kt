package com.focusritual.app.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ProtectFocusCard(
    isConfigured: Boolean,
    blockedAppCount: Int,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEditBlockedApps: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier,
) { }
