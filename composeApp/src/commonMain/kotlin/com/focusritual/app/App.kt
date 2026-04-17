package com.focusritual.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusritual.app.core.designsystem.theme.FocusRitualEasing
import com.focusritual.app.core.designsystem.theme.FocusRitualTheme
import com.focusritual.app.core.liveactivity.LiveActivityEffect
import com.focusritual.app.feature.mixer.MixerIntent
import com.focusritual.app.feature.mixer.MixerScreen
import com.focusritual.app.feature.mixer.MixerViewModel
import com.focusritual.app.feature.session.FocusSessionScreen
import com.focusritual.app.feature.session.SessionConfig
import com.focusritual.app.feature.timer.ActiveSessionIntent
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
            onTogglePause = {
                if (activeScreen != null) {
                    sessionViewModel?.onIntent(ActiveSessionIntent.TogglePause)
                } else {
                    mixerViewModel.onIntent(MixerIntent.TogglePlayback)
                }
            },
            onStopMix = {
                if (mixerState.isPlaying) {
                    mixerViewModel.onIntent(MixerIntent.TogglePlayback)
                }
            },
            onSkipPhase = {
                sessionViewModel?.onIntent(ActiveSessionIntent.Skip)
            },
            onEndSession = {
                sessionViewModel?.onIntent(ActiveSessionIntent.Stop)
                if (mixerState.isPlaying) {
                    mixerViewModel.onIntent(MixerIntent.TogglePlayback)
                }
                sessionKey++
                currentScreen = AppScreen.Mixer
            },
        )

        AnimatedContent(
            targetState = currentScreen,
            contentAlignment = Alignment.Center,
            transitionSpec = {
                val initial = initialState
                val target = targetState
                when {
                    // Mixer -> FocusSession : "A thought surfaces" — rises with weight from beneath
                    initial is AppScreen.Mixer && target is AppScreen.FocusSession -> {
                        val enter = slideInVertically(
                            animationSpec = tween(480, easing = FocusRitualEasing.DeepEaseOut),
                        ) { (it * 0.22f).toInt() } +
                            scaleIn(
                                animationSpec = tween(480, easing = FocusRitualEasing.DeepEaseOut),
                                initialScale = 0.97f,
                            ) +
                            fadeIn(tween(300, delayMillis = 60))
                        val exit = scaleOut(
                            animationSpec = tween(400, easing = FocusRitualEasing.Atmospheric),
                            targetScale = 0.97f,
                        ) + fadeOut(tween(360, easing = FocusRitualEasing.Atmospheric))
                        enter togetherWith exit
                    }

                    // FocusSession -> Mixer : "Dissolves back into the forest" — panel softens, world breathes back
                    initial is AppScreen.FocusSession && target is AppScreen.Mixer -> {
                        val enter = scaleIn(
                            animationSpec = tween(500, easing = FocusRitualEasing.Atmospheric),
                            initialScale = 1.02f,
                        ) + fadeIn(tween(420, easing = FocusRitualEasing.Atmospheric))
                        val exit = scaleOut(
                            animationSpec = tween(340, easing = FocusRitualEasing.CinematicIn),
                            targetScale = 0.96f,
                        ) + fadeOut(tween(280, easing = FocusRitualEasing.CinematicIn))
                        enter togetherWith exit
                    }

                    // FocusSession -> ActiveSession : "The ritual begins" — beat of darkness, then presence
                    initial is AppScreen.FocusSession && target is AppScreen.ActiveSession -> {
                        val enter = scaleIn(
                            animationSpec = tween(600, easing = FocusRitualEasing.Ritual),
                            initialScale = 0.96f,
                        ) + fadeIn(tween(500, delayMillis = 80))
                        val exit = fadeOut(tween(300, easing = FocusRitualEasing.CinematicIn))
                        enter togetherWith exit
                    }

                    // ActiveSession -> Mixer : "Emerging from depth" — emotional payoff, world expands
                    initial is AppScreen.ActiveSession && target is AppScreen.Mixer -> {
                        val enter = scaleIn(
                            animationSpec = tween(700, easing = FocusRitualEasing.Atmospheric),
                            initialScale = 1.04f,
                        ) + fadeIn(tween(600, easing = FocusRitualEasing.Atmospheric))
                        // ActiveSession already internally fades over ~400ms before onFinish() is called.
                        // Keep this outer exit short — do not double-fade.
                        val exit = fadeOut(tween(200))
                        enter togetherWith exit
                    }

                    // Fallback — clean, neutral
                    else ->
                        fadeIn(tween(300, easing = FocusRitualEasing.Atmospheric)) togetherWith
                            fadeOut(tween(260, easing = FocusRitualEasing.CinematicIn))
                }.using(SizeTransform(clip = false))
            },
            label = "AppScreenTransition",
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
                        if (currentScreen is AppScreen.ActiveSession) {
                            sessionKey++
                            currentScreen = AppScreen.Mixer
                        }
                    },
                    onSoundControl = mixerViewModel::setSessionMasterVolume,
                )
            }
        }
    }
}