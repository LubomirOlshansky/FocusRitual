package com.focusritual.app.feature.mixer.data

import com.focusritual.app.feature.mixer.domain.SavedSound
import kotlinx.serialization.Serializable

@Serializable
data class AmbientSnapshot(
    val sounds: List<SavedSound>,
    val loadedPresetId: String? = null,
)

class AmbientStateRepository(private val store: JsonStore = JsonStore()) {

    fun read(): AmbientSnapshot? = store.read<AmbientSnapshot>(KEY)

    fun write(snapshot: AmbientSnapshot) {
        store.write(KEY, snapshot)
    }

    companion object {
        private const val KEY = "mixer.ambient_snapshot.v1"
    }
}
