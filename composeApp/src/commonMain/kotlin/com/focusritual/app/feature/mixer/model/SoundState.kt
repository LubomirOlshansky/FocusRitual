package com.focusritual.app.feature.mixer.model

enum class SoundCategory { ALL, NATURE, WEATHER, PLACES, NOISE }

val SoundCategory.displayName: String
    get() = when (this) {
        SoundCategory.ALL -> "All"
        SoundCategory.NATURE -> "Nature"
        SoundCategory.WEATHER -> "Weather"
        SoundCategory.PLACES -> "Places"
        SoundCategory.NOISE -> "Noise"
    }

data class SoundState(
    val id: String,
    val name: String,
    val icon: SoundIcon,
    val category: SoundCategory,
    val isEnabled: Boolean = false,
    val volume: Float = 0.5f,
    val organicMotion: Boolean = false,
    val liveVolume: Float? = null,
)

enum class SoundIcon {
    Rain, Thunder, Wind, Forest, Stream, Cafe, Fireplace, BrownNoise, Waves
}

fun defaultSounds(): List<SoundState> = listOf(
    SoundState(id = "rain", name = "Rain", icon = SoundIcon.Rain, category = SoundCategory.WEATHER, isEnabled = true, volume = 0.7f),
    SoundState(id = "thunder", name = "Thunder", icon = SoundIcon.Thunder, category = SoundCategory.WEATHER),
    SoundState(id = "wind", name = "Wind", icon = SoundIcon.Wind, category = SoundCategory.NATURE, isEnabled = true, volume = 0.5f),
    SoundState(id = "forest", name = "Forest", icon = SoundIcon.Forest, category = SoundCategory.NATURE),
    SoundState(id = "stream", name = "Stream", icon = SoundIcon.Stream, category = SoundCategory.NATURE),
    SoundState(id = "cafe", name = "Cafe", icon = SoundIcon.Cafe, category = SoundCategory.PLACES),
    SoundState(id = "fireplace", name = "Fireplace", icon = SoundIcon.Fireplace, category = SoundCategory.PLACES),
    SoundState(id = "brown_noise", name = "Brown Noise", icon = SoundIcon.BrownNoise, category = SoundCategory.NOISE),
    SoundState(id = "waves", name = "Waves", icon = SoundIcon.Waves, category = SoundCategory.NATURE),
)
