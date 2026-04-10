package com.focusritual.app.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ProtectFocusCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
