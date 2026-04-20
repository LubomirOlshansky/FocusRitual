package com.focusritual.app.core.audio

fun interface AudioPlayerFactory {
    fun create(): AudioPlayerHandle
}

object DefaultAudioPlayerFactory : AudioPlayerFactory {
    override fun create(): AudioPlayerHandle = RealAudioPlayerHandle()
}

private class RealAudioPlayerHandle : AudioPlayerHandle {
    private val delegate = AudioPlayer()
    override fun play(data: ByteArray, loop: Boolean) = delegate.play(data, loop)
    override fun stop() = delegate.stop()
    override fun setVolume(volume: Float) = delegate.setVolume(volume)
    override fun release() = delegate.release()
    override val isPlaying: Boolean get() = delegate.isPlaying
}
