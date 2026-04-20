package com.focusritual.app.core.audio

class SoundMixer(private val factory: AudioPlayerFactory = DefaultAudioPlayerFactory) {
    private val players = mutableMapOf<String, AudioPlayerHandle>()
    private val loadedData = mutableMapOf<String, ByteArray>()

    fun cacheAudioData(soundId: String, data: ByteArray) {
        loadedData[soundId] = data
    }

    fun syncState(commands: List<AudioCommand>) {
        for (cmd in commands) {
            val data = loadedData[cmd.id]
            val player = players[cmd.id]

            if (cmd.enabled && data != null) {
                if (player == null) {
                    val newPlayer = factory.create()
                    players[cmd.id] = newPlayer
                    newPlayer.play(data)
                    newPlayer.setVolume(cmd.volume)
                } else if (!player.isPlaying) {
                    player.play(data)
                    player.setVolume(cmd.volume)
                } else {
                    player.setVolume(cmd.volume)
                }
            } else {
                player?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                }
                if (!cmd.enabled) {
                    player?.release()
                    players.remove(cmd.id)
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
