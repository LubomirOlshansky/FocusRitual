package com.focusritual.app

import androidx.compose.runtime.Composable
import com.focusritual.app.core.designsystem.theme.FocusRitualTheme
import com.focusritual.app.feature.mixer.MixerScreen

@Composable
fun App() {
    FocusRitualTheme {
        MixerScreen()
    }
}