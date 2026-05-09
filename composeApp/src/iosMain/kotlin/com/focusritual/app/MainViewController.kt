package com.focusritual.app

import androidx.compose.ui.window.ComposeUIViewController
import com.focusritual.app.core.platformaction.ProvidePlatformActions

fun MainViewController() = ComposeUIViewController {
    ProvidePlatformActions {
        App()
    }
}