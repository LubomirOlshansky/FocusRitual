package com.focusritual.app.feature.mixer.model

data class SoundState(
    val id: String,
    val name: String,
    val icon: SoundIcon,
    val isEnabled: Boolean = false,
    val volume: Float = 0.5f,
)

enum class SoundIcon {
    Rain, Thunder, Wind, Forest, Stream, Cafe, Fireplace, BrownNoise
}

fun defaultSounds(): List<SoundState> = listOf(
    SoundState(id = "rain", name = "Rain", icon = SoundIcon.Rain, isEnabled = true, volume = 0.7f),
    SoundState(id = "thunder", name = "Thunder", icon = SoundIcon.Thunder),
    SoundState(id = "wind", name = "Wind", icon = SoundIcon.Wind, isEnabled = true, volume = 0.5f),
    SoundState(id = "forest", name = "Forest", icon = SoundIcon.Forest),
    SoundState(id = "stream", name = "Stream", icon = SoundIcon.Stream),
    SoundState(id = "cafe", name = "Cafe", icon = SoundIcon.Cafe),
    SoundState(id = "fireplace", name = "Fireplace", icon = SoundIcon.Fireplace),
    SoundState(id = "brown_noise", name = "Brown Noise", icon = SoundIcon.BrownNoise),
)
