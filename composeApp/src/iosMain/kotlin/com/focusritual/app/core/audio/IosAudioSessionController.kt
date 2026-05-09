@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.focusritual.app.core.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryOptions
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeEnded
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.setActive
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue

internal object IosAudioSessionController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val settings = AudioSettingsRepository.Default

    private var observerStarted = false
    private var activeCount = 0
    private var playbackSettings = AudioPlaybackSettings(
        mixWithOthersEnabled = settings.mixWithOthersEnabled.value,
        duckOthersEnabled = settings.duckOthersEnabled.value,
        mixWithOthersVolume = settings.mixWithOthersVolume.value,
        duckLevel = settings.duckLevel.value,
    )
    private var interruptionObserver: Any? = null
    private var wasPlayingBeforeInterruption = false

    fun onPlaybackStarted() {
        activeCount += 1
        ensureSettingsObserver()
        ensureInterruptionObserver()
        applySessionSettings(active = true)
    }

    fun onPlaybackStopped() {
        activeCount = (activeCount - 1).coerceAtLeast(0)
        if (activeCount == 0) {
            wasPlayingBeforeInterruption = false
            settings.setExternalAudioAttenuation(1f)
            AVAudioSession.sharedInstance().setActive(false, error = null)
        }
    }

    private fun ensureSettingsObserver() {
        if (observerStarted) return
        observerStarted = true
        scope.launch {
            settings.playbackSettings.collect { nextSettings ->
                playbackSettings = nextSettings
                if (activeCount > 0) {
                    applySessionSettings(active = true)
                }
            }
        }
    }

    private fun ensureInterruptionObserver() {
        if (interruptionObserver != null) return
        
        interruptionObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVAudioSessionInterruptionNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { notification ->
            val notif = notification ?: return@addObserverForName
            val userInfo = notif.userInfo ?: return@addObserverForName
            val typeValue = (userInfo[AVAudioSessionInterruptionTypeKey] as? NSNumber)?.unsignedIntegerValue ?: return@addObserverForName
            
            when (typeValue.toInt()) {
                AVAudioSessionInterruptionTypeBegan.toInt() -> {
                    // Interruption began - mute audio
                    wasPlayingBeforeInterruption = activeCount > 0
                    if (playbackSettings.mixWithOthersEnabled && playbackSettings.duckOthersEnabled) {
                        settings.setExternalAudioAttenuation(playbackSettings.duckLevel)
                    } else {
                        settings.setExternalAudioAttenuation(0f)
                    }
                }
                AVAudioSessionInterruptionTypeEnded.toInt() -> {
                    // Interruption ended - restore audio if was playing
                    if (wasPlayingBeforeInterruption) {
                        settings.setExternalAudioAttenuation(1f)
                    }
                }
            }
        }
    }

    private fun applySessionSettings(active: Boolean) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            category = AVAudioSessionCategoryPlayback,
            withOptions = playbackSettings.toOptions(),
            error = null,
        )
        session.setActive(active, error = null)
    }

    private fun AudioPlaybackSettings.toOptions(): AVAudioSessionCategoryOptions {
        var options: AVAudioSessionCategoryOptions = 0u
        if (mixWithOthersEnabled) {
            options = options or AVAudioSessionCategoryOptionMixWithOthers
        }
        // Do NOT use AVAudioSessionCategoryOptionDuckOthers - we duck ourselves, not other apps
        return options
    }
}
