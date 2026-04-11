package com.focusritual.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.theme.FocusRitualTheme
import com.focusritual.app.core.liveactivity.LiveActivityEffect
import com.focusritual.app.feature.mixer.MixerScreen
import com.focusritual.app.feature.mixer.MixerViewModel
import com.focusritual.app.feature.session.FocusSessionScreen
import com.focusritual.app.feature.session.SessionConfig
import com.focusritual.app.feature.timer.ActiveSessionScreen
import com.focusritual.app.feature.timer.ActiveSessionViewModel

sealed interface AppScreen {
    data object Mixer : AppScreen
    data object FocusSession : AppScreen
    data class ActiveSession(val config: SessionConfig, val sessionId: Int = 0) : AppScreen
}

@Composable
fun App() {
    FocusRitualTheme {
        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Mixer) }
        var sessionKey by remember { mutableIntStateOf(0) }
        val mixerViewModel: MixerViewModel = viewModel { MixerViewModel() }
        val mixerState by mixerViewModel.uiState.collectAsStateWithLifecycle()

        // Track session ViewModel at this level for Live Activity
        val activeScreen = currentScreen as? AppScreen.ActiveSession
        val sessionViewModel: ActiveSessionViewModel? = activeScreen?.let {
            viewModel(key = "session_${it.sessionId}") { ActiveSessionViewModel(it.config) }
        }
        val sessionState = sessionViewModel?.uiState?.collectAsStateWithLifecycle()

        // Live Activity sync — iOS-only, no-op on Android
        LiveActivityEffect(
            mixerState = mixerState,
            sessionState = sessionState?.value,
            isSessionActive = activeScreen != null,
        )

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
                    onStartSession = { config -> currentScreen = AppScreen.ActiveSession(config, sessionKey) },
                )
                is AppScreen.ActiveSession -> ActiveSessionScreen(
                    config = screen.config,
                    sessionKey = screen.sessionId,
                    onFinish = {
                        sessionKey++
                        currentScreen = AppScreen.Mixer
                    },
                    onSoundControl = mixerViewModel::setSessionMasterVolume,
                )
            }
        }
    }
}