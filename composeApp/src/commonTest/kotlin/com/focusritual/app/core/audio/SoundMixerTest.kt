package com.focusritual.app.core.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

private sealed interface Event {
    data class Play(val data: ByteArray, val loop: Boolean) : Event
    data object Stop : Event
    data class SetVolume(val v: Float) : Event
    data object Release : Event
}

private class FakeAudioPlayer : AudioPlayerHandle {
    val events = mutableListOf<Event>()
    private var _isPlaying: Boolean = false
    override val isPlaying: Boolean get() = _isPlaying

    override fun play(data: ByteArray, loop: Boolean) {
        events += Event.Play(data, loop); _isPlaying = true
    }
    override fun stop() { events += Event.Stop; _isPlaying = false }
    override fun setVolume(volume: Float) { events += Event.SetVolume(volume) }
    override fun release() { events += Event.Release; _isPlaying = false }
}

private class FakeAudioPlayerFactory : AudioPlayerFactory {
    val created = mutableListOf<FakeAudioPlayer>()
    var lastCreated: FakeAudioPlayer? = null
    override fun create(): AudioPlayerHandle {
        val p = FakeAudioPlayer()
        created += p
        lastCreated = p
        return p
    }
}

class SoundMixerTest {

    @Test fun first_enabled_command_creates_player_plays_and_sets_volume() {
        val factory = FakeAudioPlayerFactory()
        val mixer = SoundMixer(factory)
        val bytes = byteArrayOf(1, 2, 3)
        mixer.cacheAudioData("rain", bytes)

        mixer.syncState(listOf(AudioCommand("rain", volume = 0.7f, enabled = true)))

        assertEquals(1, factory.created.size)
        val p = factory.lastCreated!!
        assertEquals(2, p.events.size)
        val playEvt = p.events[0] as Event.Play
        assertTrue(playEvt.data.contentEquals(bytes))
        assertEquals(true, playEvt.loop)
        assertEquals(Event.SetVolume(0.7f), p.events[1])
        assertTrue(p.isPlaying)
    }

    @Test fun subsequent_enabled_command_does_not_recreate_only_setVolume() {
        val factory = FakeAudioPlayerFactory()
        val mixer = SoundMixer(factory)
        mixer.cacheAudioData("rain", byteArrayOf(1))
        mixer.syncState(listOf(AudioCommand("rain", 0.5f, enabled = true)))
        val p = factory.lastCreated!!
        val before = factory.created.size

        mixer.syncState(listOf(AudioCommand("rain", 0.9f, enabled = true)))

        assertEquals(before, factory.created.size, "no new player created")
        // Last event must be the new setVolume.
        assertEquals(Event.SetVolume(0.9f), p.events.last())
        // Only 1 Play in total.
        assertEquals(1, p.events.count { it is Event.Play })
    }

    @Test fun disabling_stops_releases_and_removes_so_re_enable_creates_new_player() {
        val factory = FakeAudioPlayerFactory()
        val mixer = SoundMixer(factory)
        mixer.cacheAudioData("rain", byteArrayOf(1))
        mixer.syncState(listOf(AudioCommand("rain", 0.5f, true)))
        val first = factory.lastCreated!!

        mixer.syncState(listOf(AudioCommand("rain", 0.5f, false)))
        assertTrue(first.events.contains(Event.Stop), "stop emitted")
        assertTrue(first.events.contains(Event.Release), "release emitted")
        assertFalse(first.isPlaying)

        // Re-enabling must create a fresh player (proves it was removed from the map).
        mixer.syncState(listOf(AudioCommand("rain", 0.5f, true)))
        assertEquals(2, factory.created.size)
        val second = factory.lastCreated!!
        assertNotSame(first, second)
        assertTrue(second.isPlaying)
    }

    @Test fun release_releases_all_players_and_clears() {
        val factory = FakeAudioPlayerFactory()
        val mixer = SoundMixer(factory)
        mixer.cacheAudioData("rain", byteArrayOf(1))
        mixer.cacheAudioData("wind", byteArrayOf(2))
        mixer.syncState(
            listOf(
                AudioCommand("rain", 0.5f, true),
                AudioCommand("wind", 0.5f, true),
            )
        )
        val a = factory.created[0]
        val b = factory.created[1]

        mixer.release()
        assertTrue(a.events.contains(Event.Release))
        assertTrue(b.events.contains(Event.Release))

        // After release, a fresh syncState (with cached data gone) cannot create a player.
        mixer.syncState(listOf(AudioCommand("rain", 0.5f, true)))
        assertEquals(2, factory.created.size, "no new player because cache cleared")
    }

    @Test fun volume_is_applied_verbatim_caller_already_master_scaled() {
        val factory = FakeAudioPlayerFactory()
        val mixer = SoundMixer(factory)
        mixer.cacheAudioData("rain", byteArrayOf(1))
        mixer.syncState(listOf(AudioCommand("rain", volume = 0.5f, enabled = true)))
        val p = assertNotNull(factory.lastCreated)
        assertTrue(p.events.any { it is Event.SetVolume && it.v == 0.5f })
    }
}
