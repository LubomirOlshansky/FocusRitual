package com.focusritual.app.core.audio

interface AudioPlayerHandle {
    fun play(data: ByteArray, loop: Boolean = true)
    fun stop()
    fun setVolume(volume: Float)
    fun release()
    val isPlaying: Boolean
}
