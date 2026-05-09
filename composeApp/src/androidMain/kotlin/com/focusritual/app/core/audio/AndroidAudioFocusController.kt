package com.focusritual.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal object AndroidAudioFocusController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val settings = AudioSettingsRepository.Default
    
    private var observerStarted = false
    private var playbackActive = false
    private var mixWithOthersEnabled = settings.mixWithOthersEnabled.value
    private var duckOthersEnabled = settings.duckOthersEnabled.value
    private var duckLevel = settings.duckLevel.value
    private var activeFocusRequest: AudioFocusRequest? = null
    private var activeFocusGain: Int? = null
    private var wasPlayingBeforeInterruption = false

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                settings.setExternalAudioAttenuation(0f)
                wasPlayingBeforeInterruption = playbackActive
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mixWithOthersEnabled && duckOthersEnabled) {
                    settings.setExternalAudioAttenuation(duckLevel)
                } else {
                    settings.setExternalAudioAttenuation(0f)
                }
                wasPlayingBeforeInterruption = playbackActive
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                settings.setExternalAudioAttenuation(1f)
            }
        }
    }

    fun onPlaybackStarted() {
        playbackActive = true
        ensureSettingsObserver()
        updateFocusRequest()
    }

    fun onPlaybackStopped() {
        playbackActive = false
        wasPlayingBeforeInterruption = false
        settings.setExternalAudioAttenuation(1f)
        abandonFocus()
    }

    private fun ensureSettingsObserver() {
        if (observerStarted) return
        observerStarted = true
        scope.launch {
            settings.playbackSettings.collect { playbackSettings ->
                mixWithOthersEnabled = playbackSettings.mixWithOthersEnabled
                duckOthersEnabled = playbackSettings.duckOthersEnabled
                duckLevel = playbackSettings.duckLevel
                if (playbackActive) {
                    updateFocusRequest()
                }
            }
        }
    }

    private fun updateFocusRequest() {
        if (!playbackActive) return
        
        // If mix is enabled and duck is disabled, abandon focus to coexist freely
        if (mixWithOthersEnabled && !duckOthersEnabled) {
            abandonFocus()
            settings.setExternalAudioAttenuation(1f)
            return
        }

        // Request AUDIOFOCUS_GAIN (not TRANSIENT_MAY_DUCK, which would duck other apps)
        val focusGain = AudioManager.AUDIOFOCUS_GAIN
        if (activeFocusRequest != null && activeFocusGain == focusGain) return

        abandonFocus()
        val request = AudioFocusRequest.Builder(focusGain)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
        audioManager.requestAudioFocus(request)
        activeFocusRequest = request
        activeFocusGain = focusGain
    }

    private fun abandonFocus() {
        activeFocusRequest?.let { request ->
            audioManager.abandonAudioFocusRequest(request)
        }
        activeFocusRequest = null
        activeFocusGain = null
    }

    private val audioManager: AudioManager
        get() = AndroidAudioContext.appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
}