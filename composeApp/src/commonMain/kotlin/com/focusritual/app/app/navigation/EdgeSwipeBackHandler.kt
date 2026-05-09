package com.focusritual.app.app.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun EdgeSwipeBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
