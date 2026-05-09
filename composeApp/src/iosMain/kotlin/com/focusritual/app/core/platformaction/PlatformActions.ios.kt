package com.focusritual.app.core.platformaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPlatformActions(): PlatformActions = remember { IosPlatformActions() }
