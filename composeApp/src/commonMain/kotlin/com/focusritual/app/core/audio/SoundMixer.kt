package com.focusritual.app.core.audio

import com.focusritual.app.feature.mixer.model.SoundState

class SoundMixer {
    private val players = mutableMapOf<String, AudioPlayer>()
    private val loadedData = mutableMapOf<String, ByteArray>()

    fun cacheAudioData(soundId: String, data: ByteArray) {
        loadedData[soundId] = data
    }

    fun syncState(sounds: List<SoundState>, isPlaying: Boolean) {
        for (sound in sounds) {
            val data = loadedData[sound.id]
            val player = players[sound.id]

            if (isPlaying && sound.isEnabled && data != null) {
                if (player == null) {
                    val newPlayer = AudioPlayer()
                    players[sound.id] = newPlayer
                    newPlayer.play(data)
                    newPlayer.setVolume(sound.volume)
                } else if (!player.isPlaying) {
                    player.play(data)
                    player.setVolume(sound.volume)
                } else {
                    player.setVolume(sound.volume)
                }
            } else {
                player?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                }
                if (!sound.isEnabled || !isPlaying) {
                    player?.release()
                    players.remove(sound.id)
                }
            }
        }
    }

    fun release() {
        players.values.forEach { it.release() }
        players.clear()
        loadedData.clear()
    }
}
