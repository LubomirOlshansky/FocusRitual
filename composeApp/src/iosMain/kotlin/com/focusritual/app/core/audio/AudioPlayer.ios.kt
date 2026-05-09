package com.focusritual.app.core.audio

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

actual class AudioPlayer actual constructor() {
    private var avPlayer: AVAudioPlayer? = null
    private var sessionStarted = false

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun play(data: ByteArray, loop: Boolean) {
        release()

        IosAudioSessionController.onPlaybackStarted()
        sessionStarted = true

        val nsData = data.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = data.size.toULong(),
            )
        }

        avPlayer = AVAudioPlayer(data = nsData, error = null).apply {
            numberOfLoops = if (loop) -1 else 0
            prepareToPlay()
            play()
        }
    }

    actual fun stop() {
        avPlayer?.stop()
    }

    actual fun setVolume(volume: Float) {
        avPlayer?.volume = volume.coerceIn(0f, 1f)
    }

    actual fun release() {
        avPlayer?.stop()
        avPlayer = null
        if (sessionStarted) {
            sessionStarted = false
            IosAudioSessionController.onPlaybackStopped()
        }
    }

    actual val isPlaying: Boolean
        get() = avPlayer?.playing == true
}
