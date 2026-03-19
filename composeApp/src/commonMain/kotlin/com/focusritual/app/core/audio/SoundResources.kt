package com.focusritual.app.core.audio

object SoundResources {
    private val soundFiles = mapOf(
        "rain" to "files/rain.m4a",
        "thunder" to "files/thunder.m4a",
        "wind" to "files/wind.m4a",
        "forest" to "files/forest.m4a",
        "stream" to "files/stream.m4a",
        "cafe" to "files/cafe.m4a",
        "fireplace" to "files/fireplace.m4a",
        "brown_noise" to "files/brown_noise.m4a",
        "waves" to "files/waves.m4a",
    )

    fun getResourcePath(soundId: String): String? = soundFiles[soundId]

    fun hasResource(soundId: String): Boolean = soundId in soundFiles
}
