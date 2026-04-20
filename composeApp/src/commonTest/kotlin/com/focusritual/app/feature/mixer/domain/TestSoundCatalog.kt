package com.focusritual.app.feature.mixer.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.focusritual.app.feature.mixer.domain.SoundCategory

internal val TestIcon: ImageVector = Icons.Filled.Star

internal class TestSoundCatalog(
    private val defs: List<SoundDefinition> = defaultDefs(),
) : SoundCatalog {
    override fun all(): List<SoundDefinition> = defs
    override suspend fun loadAudioBytes(definition: SoundDefinition): ByteArray? = null

    companion object {
        fun defaultDefs(): List<SoundDefinition> = listOf(
            SoundDefinition("rain", "Rain", SoundCategory.WEATHER, TestIcon, null),
            SoundDefinition("wind", "Wind", SoundCategory.NATURE, TestIcon, null),
            SoundDefinition("cafe", "Cafe", SoundCategory.PLACES, TestIcon, null),
        )
    }
}
