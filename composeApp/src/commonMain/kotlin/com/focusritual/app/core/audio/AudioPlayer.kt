package com.focusritual.app.core.audio

expect class AudioPlayer() {
    fun play(data: ByteArray, loop: Boolean = true)
    fun stop()
    fun setVolume(volume: Float)
    fun release()
    val isPlaying: Boolean
}
