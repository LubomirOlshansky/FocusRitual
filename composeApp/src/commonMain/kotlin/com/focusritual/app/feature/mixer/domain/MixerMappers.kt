package com.focusritual.app.feature.mixer.domain


/**
 * Pure mapper extracted verbatim from MixerViewModel.withDerivedFields().
 * Produces the LiveActivity-facing summary fields.
 */
fun summarizeActiveMix(sounds: List<SoundState>, isPlaying: Boolean): CurrentMixSummary {
    val enabled = sounds.filter { it.isEnabled }
    val count = enabled.size
    val summary = when {
        count == 0 -> ""
        count <= 2 -> enabled.joinToString(" • ") { it.name }
        else -> enabled.take(2).joinToString(" • ") { it.name } + " • +${count - 2}"
    }
    return CurrentMixSummary(
        isPlaying = isPlaying,
        activeSoundCount = count,
        activeSoundsSummary = summary,
    )
}

/**
 * Pure mapper extracted verbatim from MixerViewModel.filteredSounds.
 * Preserves LinkedHashMap ordering (SoundCategory.entries minus ALL) and the
 * special-case ALL behaviour (no category filter).
 */
fun groupActiveSounds(sounds: List<SoundState>, selectedCategory: SoundCategory): GroupedSounds {
    val filtered = if (selectedCategory == SoundCategory.ALL) sounds
                   else sounds.filter { it.category == selectedCategory }
    val grouped = filtered.groupBy { it.category }
    val byCategory = linkedMapOf<SoundCategory, List<SoundState>>().apply {
        SoundCategory.entries.filter { it != SoundCategory.ALL }.forEach { cat ->
            grouped[cat]?.let { put(cat, it) }
        }
    }
    return GroupedSounds(byCategory = byCategory)
}
