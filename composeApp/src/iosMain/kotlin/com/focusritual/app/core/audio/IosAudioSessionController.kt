@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.focusritual.app.core.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryOptions
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionOptionKey
import platform.AVFAudio.AVAudioSessionInterruptionOptionShouldResume
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeEnded
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionSilenceSecondaryAudioHintNotification
import platform.AVFAudio.AVAudioSessionSilenceSecondaryAudioHintTypeBegin
import platform.AVFAudio.AVAudioSessionSilenceSecondaryAudioHintTypeEnd
import platform.AVFAudio.AVAudioSessionSilenceSecondaryAudioHintTypeKey
import platform.AVFAudio.isOtherAudioPlaying
import platform.AVFAudio.secondaryAudioShouldBeSilencedHint
import platform.AVFAudio.setActive
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue


private const val ExternalAudioPollIntervalMillis = 750L

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
    private var silenceHintObserver: Any? = null
    private var externalAudioPollingJob: Job? = null
    private var wasPlayingBeforeInterruption = false

    fun onPlaybackStarted() {
        activeCount += 1
        ensureSettingsObserver()
        ensureInterruptionObserver()
        ensureSilenceHintObserver()
        applySessionSettings(active = true)
        updateExternalAudioPolling()
    }

    fun onPlaybackStopped() {
        activeCount = (activeCount - 1).coerceAtLeast(0)
        if (activeCount == 0) {
            wasPlayingBeforeInterruption = false
            settings.setExternalAudioAttenuation(1f)
            updateExternalAudioPolling()
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
                updateExternalAudioPolling()
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
            val session = AVAudioSession.sharedInstance()
            val optionValue = (userInfo[AVAudioSessionInterruptionOptionKey] as? NSNumber)?.unsignedIntegerValue ?: 0UL
            val shouldResume = (optionValue and AVAudioSessionInterruptionOptionShouldResume) != 0UL

            when (typeValue.toInt()) {
                AVAudioSessionInterruptionTypeBegan.toInt() -> {
                    wasPlayingBeforeInterruption = activeCount > 0
                    val targetAttenuation = if (playbackSettings.mixWithOthersEnabled && playbackSettings.duckOthersEnabled) {
                        playbackSettings.duckLevel
                    } else {
                        1f
                    }
                    settings.setExternalAudioAttenuation(targetAttenuation)
                }
                AVAudioSessionInterruptionTypeEnded.toInt() -> {
                    val willResume = shouldResume && wasPlayingBeforeInterruption
                    if (willResume) {
                        session.setActive(true, error = null)
                        settings.setExternalAudioAttenuation(1f)
                        updateExternalAudioPolling()
                        settings.triggerResumeTick()
                    }
                    wasPlayingBeforeInterruption = false
                }
            }
        }
    }

    private fun ensureSilenceHintObserver() {
        if (silenceHintObserver != null) return

        silenceHintObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVAudioSessionSilenceSecondaryAudioHintNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { notification ->
            val userInfo = notification?.userInfo
            val rawType = (userInfo?.get(AVAudioSessionSilenceSecondaryAudioHintTypeKey) as? NSNumber)?.unsignedIntegerValue?.toInt()
            if (!requiresExternalAudioPolling()) return@addObserverForName
            when (rawType) {
                AVAudioSessionSilenceSecondaryAudioHintTypeBegin.toInt() ->
                    settings.setExternalAudioAttenuation(playbackSettings.duckLevel)
                AVAudioSessionSilenceSecondaryAudioHintTypeEnd.toInt() ->
                    settings.setExternalAudioAttenuation(1f)
            }
        }
    }

    private fun updateExternalAudioPolling() {
        if (requiresExternalAudioPolling()) {
            updateExternalAudioAttenuation()
            if (externalAudioPollingJob?.isActive == true) return

            externalAudioPollingJob = scope.launch {
                delay(ExternalAudioPollIntervalMillis)
                while (requiresExternalAudioPolling()) {
                    updateExternalAudioAttenuation()
                    delay(ExternalAudioPollIntervalMillis)
                }
            }
        } else {
            externalAudioPollingJob?.cancel()
            externalAudioPollingJob = null
            updateExternalAudioAttenuation()
        }
    }

    private fun requiresExternalAudioPolling(): Boolean =
        activeCount > 0 && playbackSettings.mixWithOthersEnabled && playbackSettings.duckOthersEnabled

    private fun updateExternalAudioAttenuation() {
        val session = AVAudioSession.sharedInstance()
        val otherAudioPlaying = session.isOtherAudioPlaying()
        val silenceHint = session.secondaryAudioShouldBeSilencedHint()
        val otherAudioActive = otherAudioPlaying || silenceHint
        val shouldSelfDuck = requiresExternalAudioPolling() && otherAudioActive
        val targetAttenuation = if (shouldSelfDuck) playbackSettings.duckLevel else 1f

        if (settings.externalAudioAttenuation.value != targetAttenuation) {
            settings.setExternalAudioAttenuation(targetAttenuation)
        }
    }

    private fun applySessionSettings(active: Boolean) {
        val session = AVAudioSession.sharedInstance()
        val options = playbackSettings.toOptions()
        session.setCategory(
            category = AVAudioSessionCategoryPlayback,
            withOptions = options,
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
