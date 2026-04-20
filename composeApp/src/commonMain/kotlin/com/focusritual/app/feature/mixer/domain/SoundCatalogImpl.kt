package com.focusritual.app.feature.mixer.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import focusritual.composeapp.generated.resources.Res

class SoundCatalogImpl : SoundCatalog {

    private val definitions: List<SoundDefinition> = listOf(
        SoundDefinition("rain", "Rain", SoundCategory.WEATHER, Icons.Filled.WaterDrop, "files/rain.m4a"),
        SoundDefinition("thunder", "Thunder", SoundCategory.WEATHER, Icons.Filled.Thunderstorm, "files/thunder.m4a"),
        SoundDefinition("wind", "Wind", SoundCategory.NATURE, Icons.Filled.Air, "files/wind.m4a"),
        SoundDefinition("forest", "Forest", SoundCategory.NATURE, Icons.Filled.Forest, "files/forest.m4a"),
        SoundDefinition("stream", "Stream", SoundCategory.NATURE, Icons.Filled.Water, "files/stream.m4a"),
        SoundDefinition("cafe", "Cafe", SoundCategory.PLACES, Icons.Filled.LocalCafe, "files/cafe.m4a"),
        SoundDefinition("fireplace", "Fireplace", SoundCategory.PLACES, Icons.Filled.Fireplace, "files/fireplace.m4a"),
        SoundDefinition("brown_noise", "Brown Noise", SoundCategory.NOISE, Icons.Filled.GraphicEq, "files/brown_noise.m4a"),
        SoundDefinition("waves", "Waves", SoundCategory.NATURE, Icons.Filled.Water, "files/waves.m4a"),
    )

    override fun all(): List<SoundDefinition> = definitions

    override suspend fun loadAudioBytes(definition: SoundDefinition): ByteArray? {
        val path = definition.resourcePath ?: return null
        return try {
            Res.readBytes(path)
        } catch (_: Exception) {
            null
        }
    }
}
