package com.focusritual.app.feature.mixer.domain

import com.focusritual.app.feature.mixer.domain.SoundCategory
import com.focusritual.app.feature.mixer.domain.SoundState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun s(
    id: String,
    name: String = id,
    category: SoundCategory = SoundCategory.NATURE,
    enabled: Boolean = false,
) = SoundState(
    id = id,
    name = name,
    icon = TestIcon,
    category = category,
    isEnabled = enabled,
    volume = 0.5f,
    organicMotion = false,
    liveVolume = null,
)

class MixerMappersTest {

    @Test fun summarize_zero_active() {
        val r = summarizeActiveMix(listOf(s("rain"), s("wind")), isPlaying = false)
        assertEquals(0, r.activeSoundCount)
        assertEquals("", r.activeSoundsSummary)
        assertEquals(false, r.isPlaying)
    }

    @Test fun summarize_one_active() {
        val r = summarizeActiveMix(listOf(s("rain", "Rain", enabled = true), s("wind", "Wind")), true)
        assertEquals(1, r.activeSoundCount)
        assertEquals("Rain", r.activeSoundsSummary)
        assertEquals(true, r.isPlaying)
    }

    @Test fun summarize_two_active() {
        val r = summarizeActiveMix(
            listOf(s("rain", "Rain", enabled = true), s("wind", "Wind", enabled = true)),
            isPlaying = true,
        )
        assertEquals(2, r.activeSoundCount)
        assertEquals("Rain • Wind", r.activeSoundsSummary)
    }

    @Test fun summarize_three_active() {
        val r = summarizeActiveMix(
            listOf(
                s("rain", "Rain", enabled = true),
                s("wind", "Wind", enabled = true),
                s("cafe", "Cafe", enabled = true),
            ),
            isPlaying = true,
        )
        assertEquals(3, r.activeSoundCount)
        assertEquals("Rain • Wind • +1", r.activeSoundsSummary)
    }

    @Test fun summarize_four_active() {
        val r = summarizeActiveMix(
            listOf(
                s("rain", "Rain", enabled = true),
                s("wind", "Wind", enabled = true),
                s("cafe", "Cafe", enabled = true),
                s("forest", "Forest", enabled = true),
            ),
            isPlaying = true,
        )
        assertEquals(4, r.activeSoundCount)
        assertEquals("Rain • Wind • +2", r.activeSoundsSummary)
    }

    @Test fun group_all_returns_all_grouped() {
        val sounds = listOf(
            s("rain", category = SoundCategory.WEATHER),
            s("wind", category = SoundCategory.NATURE),
            s("cafe", category = SoundCategory.PLACES),
        )
        val r = groupActiveSounds(sounds, SoundCategory.ALL)
        assertEquals(3, r.byCategory.values.sumOf { it.size })
        assertTrue(r.byCategory.containsKey(SoundCategory.NATURE))
        assertTrue(r.byCategory.containsKey(SoundCategory.WEATHER))
        assertTrue(r.byCategory.containsKey(SoundCategory.PLACES))
    }

    @Test fun group_specific_category_only() {
        val sounds = listOf(
            s("rain", category = SoundCategory.WEATHER),
            s("wind", category = SoundCategory.NATURE),
        )
        val r = groupActiveSounds(sounds, SoundCategory.WEATHER)
        assertEquals(setOf(SoundCategory.WEATHER), r.byCategory.keys)
        assertEquals(listOf("rain"), r.byCategory[SoundCategory.WEATHER]!!.map { it.id })
    }

    @Test fun group_empty_input_empty_map() {
        val r = groupActiveSounds(emptyList(), SoundCategory.ALL)
        assertTrue(r.byCategory.isEmpty())
    }

    @Test fun group_preserves_category_order() {
        val sounds = listOf(
            s("cafe", category = SoundCategory.PLACES),
            s("rain", category = SoundCategory.WEATHER),
            s("wind", category = SoundCategory.NATURE),
        )
        val r = groupActiveSounds(sounds, SoundCategory.ALL)
        // Mapper preserves SoundCategory.entries order minus ALL: NATURE, WEATHER, PLACES, NOISE
        assertEquals(
            listOf(SoundCategory.NATURE, SoundCategory.WEATHER, SoundCategory.PLACES),
            r.byCategory.keys.toList(),
        )
    }
}
