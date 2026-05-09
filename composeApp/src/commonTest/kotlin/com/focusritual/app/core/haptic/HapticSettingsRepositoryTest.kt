package com.focusritual.app.core.haptic

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HapticSettingsRepositoryTest {
    @Test
    fun haptics_default_to_enabled() {
        val repository = HapticSettingsRepository(MapSettings())

        assertTrue(repository.hapticsEnabled.value)
    }

    @Test
    fun haptics_setting_persists_and_updates_flow() = runBlocking {
        val settings = MapSettings()
        val repository = HapticSettingsRepository(settings)

        repository.setHapticsEnabled(false)

        assertFalse(repository.hapticsEnabled.value)
        assertFalse(HapticSettingsRepository(settings).hapticsEnabled.value)

        repository.setHapticsEnabled(true)

        assertTrue(repository.hapticsEnabled.value)
        assertTrue(HapticSettingsRepository(settings).hapticsEnabled.value)
    }
}
