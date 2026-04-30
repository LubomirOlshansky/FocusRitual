package com.focusritual.app.feature.mixer.data

import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class JsonStore(
    val settings: Settings = Settings(),
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    inline fun <reified T> read(key: String): T? =
        settings.getStringOrNull(key)?.let {
            runCatching { json.decodeFromString<T>(it) }.getOrNull()
        }

    inline fun <reified T> write(key: String, value: T) {
        settings.putString(key, json.encodeToString(value))
    }
}
