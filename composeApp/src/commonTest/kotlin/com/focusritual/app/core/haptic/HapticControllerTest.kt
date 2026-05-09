package com.focusritual.app.core.haptic

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

private class RecordingHapticEngine : HapticEngine {
    val events = mutableListOf<HapticFeedbackType>()

    override fun perform(type: HapticFeedbackType) {
        events += type
    }
}

class HapticControllerTest {
    @Test
    fun sound_tile_enabled_maps_to_light_impact() {
        val engine = RecordingHapticEngine()
        val controller = HapticController(
            settingsRepository = HapticSettingsRepository(MapSettings()),
            engine = engine,
        )

        controller.soundTileEnabled()

        assertEquals(listOf(HapticFeedbackType.LightImpact), engine.events)
    }

    @Test
    fun mix_loaded_maps_to_medium_impact() {
        val engine = RecordingHapticEngine()
        val controller = HapticController(
            settingsRepository = HapticSettingsRepository(MapSettings()),
            engine = engine,
        )

        controller.mixLoaded()

        assertEquals(listOf(HapticFeedbackType.MediumImpact), engine.events)
    }

    @Test
    fun session_started_maps_to_success() {
        val engine = RecordingHapticEngine()
        val controller = HapticController(
            settingsRepository = HapticSettingsRepository(MapSettings()),
            engine = engine,
        )

        controller.sessionStarted()

        assertEquals(listOf(HapticFeedbackType.Success), engine.events)
    }

    @Test
    fun disabled_setting_suppresses_all_events() = runBlocking {
        val settingsRepository = HapticSettingsRepository(MapSettings())
        settingsRepository.setHapticsEnabled(false)
        val engine = RecordingHapticEngine()
        val controller = HapticController(
            settingsRepository = settingsRepository,
            engine = engine,
        )

        controller.soundTileEnabled()
        controller.mixLoaded()
        controller.sessionStarted()

        assertEquals(emptyList(), engine.events)
    }
}
