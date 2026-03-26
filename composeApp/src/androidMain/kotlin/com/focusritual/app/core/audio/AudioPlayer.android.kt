package com.focusritual.app.core.audio

import android.content.Intent
import android.media.MediaPlayer
import java.io.File

actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var tempFile: File? = null
    private var serviceStarted = false

    actual fun play(data: ByteArray, loop: Boolean) {
        release()
        val context = AndroidAudioContext.appContext
        val file = File(context.cacheDir, "audio_${hashCode()}.m4a")
        file.writeBytes(data)
        tempFile = file

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            isLooping = loop
            prepare()
            start()
        }

        onPlayerStarted()
        serviceStarted = true
    }

    actual fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
    }

    actual fun setVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(v, v)
    }

    actual fun release() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        tempFile?.delete()
        tempFile = null

        if (serviceStarted) {
            serviceStarted = false
            onPlayerReleased()
        }
    }

    actual val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    companion object {
        private var activeCount = 0

        private fun onPlayerStarted() {
            val context = AndroidAudioContext.appContext
            if (activeCount++ == 0) {
                context.startForegroundService(Intent(context, FocusAudioService::class.java))
            }
        }

        private fun onPlayerReleased() {
            if (--activeCount <= 0) {
                activeCount = 0
                val context = AndroidAudioContext.appContext
                context.stopService(Intent(context, FocusAudioService::class.java))
            }
        }
    }
}
