package com.focusritual.app.core.audio

import android.media.MediaPlayer
import java.io.File

actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var tempFile: File? = null

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
    }

    actual val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true
}
