package com.focusritual.app.core.audio

data class AudioCommand(
    val id: String,
    val volume: Float,
    val enabled: Boolean,
)
