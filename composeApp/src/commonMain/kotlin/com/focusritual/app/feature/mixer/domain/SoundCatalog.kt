package com.focusritual.app.feature.mixer.domain

import androidx.compose.ui.graphics.vector.ImageVector

data class SoundDefinition(
    val id: String,
    val name: String,
    val category: SoundCategory,
    val icon: ImageVector,
    val resourcePath: String?,
)

interface SoundCatalog {
    fun all(): List<SoundDefinition>
    suspend fun loadAudioBytes(definition: SoundDefinition): ByteArray?
}
