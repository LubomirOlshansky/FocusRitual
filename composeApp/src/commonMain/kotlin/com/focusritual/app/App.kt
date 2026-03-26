package com.focusritual.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.theme.FocusRitualTheme
import com.focusritual.app.feature.mixer.MixerScreen
import com.focusritual.app.feature.mixer.MixerViewModel
import com.focusritual.app.feature.session.FocusSessionScreen
import com.focusritual.app.feature.session.SessionConfig
import com.focusritual.app.feature.timer.ActiveSessionScreen

sealed interface AppScreen {
    data object Mixer : AppScreen
    data object FocusSession : AppScreen
    data class ActiveSession(val config: SessionConfig) : AppScreen
}

@Composable
fun App() {
    FocusRitualTheme {
        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Mixer) }
        val mixerViewModel: MixerViewModel = viewModel { MixerViewModel() }

        Crossfade(
            targetState = currentScreen,
            animationSpec = tween(300),
        ) { screen ->
            when (screen) {
                AppScreen.Mixer -> MixerScreen(
                    onStartSession = { currentScreen = AppScreen.FocusSession },
                    viewModel = mixerViewModel,
                )
                AppScreen.FocusSession -> FocusSessionScreen(
                    onClose = { currentScreen = AppScreen.Mixer },
                    onStartSession = { config -> currentScreen = AppScreen.ActiveSession(config) },
                )
                is AppScreen.ActiveSession -> ActiveSessionScreen(
                    config = screen.config,
                    onFinish = { currentScreen = AppScreen.Mixer },
                    onSoundControl = mixerViewModel::setSessionMasterVolume,
                )
            }
        }
    }
}