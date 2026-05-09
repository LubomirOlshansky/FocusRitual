# Mixer Persistence — Implementation Plan

Persist saved mixes + ambient state across launches using `multiplatform-settings` (already in catalog) + `kotlinx.serialization` (new). Restore-on-open always starts playing.

---

## 1. Goals & Non-Goals

**Goals**
- Saved mixes survive app kill/reinstall-less relaunch.
- Ambient state (sound enabled flags, volumes, organic motion, optional `loadedPresetId`) survives kill.
- Always start playing on launch (do not persist `isPlaying`).
- Architecture mirrors existing `MixRepository` pattern (StateFlow-owning repo, no UI knowledge).

**Non-Goals**
- Koin migration (still default-arg constructors).
- Mix editing/renaming/deletion UI (repo methods only).
- iCloud / cross-device sync.
- Android-specific DataStore.
- Schema migration framework (only versioned keys for future use).

---

## 2. Dependencies

### `gradle/libs.versions.toml`
```toml
[versions]
# add
kotlinxSerialization = "1.9.0"   # confirm latest compatible with Kotlin 2.3.0

[libraries]
# add
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
# add
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### [composeApp/build.gradle.kts](composeApp/build.gradle.kts)
```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)   // new
}

// in commonMain.dependencies { ... }
implementation(libs.kotlinx.serialization.json)   // new
```

`multiplatform-settings-no-arg` is already wired and provides `Settings()` with no-arg construction on both platforms — no further setup needed.

---

## 3. New Files

### `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/data/JsonStore.kt` *(optional helper)*
Thin wrapper around `Settings + Json` so both repos share the same instance / config.

```kotlin
internal class JsonStore(
    val settings: Settings = Settings(),
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    inline fun <reified T> read(key: String): T? =
        settings.getStringOrNull(key)?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() }

    inline fun <reified T> write(key: String, value: T) {
        settings[key] = json.encodeToString(value)
    }
}
```

### `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/data/MixPresetRepository.kt`
Owns the canonical list of saved mixes.

```kotlin
class MixPresetRepository(private val store: JsonStore = JsonStore()) {

    private val _state = MutableStateFlow(load())
    val state: StateFlow<List<MixPreset>> = _state.asStateFlow()

    fun save(preset: MixPreset) { _state.update { it + preset }; persist() }
    fun delete(id: String)     { _state.update { list -> list.filterNot { it.id == id } }; persist() }
    fun nameExists(name: String, excludingId: String? = null): Boolean {
        val n = name.trim().lowercase()
        return _state.value.any { it.name.trim().lowercase() == n && it.id != excludingId }
    }

    private fun load(): List<MixPreset> = store.read<List<MixPreset>>(KEY) ?: emptyList()
    private fun persist() { store.write(KEY, _state.value) }

    companion object { private const val KEY = "mixer.saved_mixes.v1" }
}
```

### `composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/data/AmbientStateRepository.kt`
Owns the persisted snapshot of the ambient mix.

```kotlin
@Serializable
data class AmbientSnapshot(
    val sounds: List<SavedSound>,
    val loadedPresetId: String? = null,
)

class AmbientStateRepository(private val store: JsonStore = JsonStore()) {

    fun read(): AmbientSnapshot? = store.read<AmbientSnapshot>(KEY)
    fun write(snapshot: AmbientSnapshot) { store.write(KEY, snapshot) }

    companion object { private const val KEY = "mixer.ambient_snapshot.v1" }
}
```

`isPlaying` is intentionally absent — launch always plays.

---

## 4. Files to Modify

| File | Change |
|---|---|
| [gradle/libs.versions.toml](gradle/libs.versions.toml) | Add `kotlinxSerialization` version, `kotlinx-serialization-json` library, `kotlinSerialization` plugin. |
| [composeApp/build.gradle.kts](composeApp/build.gradle.kts) | Apply `kotlinSerialization` plugin; add `kotlinx-serialization-json` to `commonMain.dependencies`. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixPreset.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixPreset.kt) | Annotate `MixPreset` and `SavedSound` with `@Serializable`. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixRepository.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixRepository.kt) | Accept optional `initialSnapshot: AmbientSnapshot?`. If non-null, hydrate by mapping snapshot sounds onto catalog defaults (drop unknown ids; for sounds not in snapshot, default to disabled). Else use existing `seed()`. No write side. |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) | Constructor takes `presetRepo: MixPresetRepository`, `ambientRepo: AmbientStateRepository` (default-args). Drop in-memory `_savedMixes` — read from `presetRepo.state`. On hydrate, validate `loadedPresetId` against `presetRepo.state.value`; drop if missing. Replace `saveCurrentMix` body to call `presetRepo.save(...)`. Add debounced ambient writer in `init`:<br>`combine(repo.state, _loadedPresetId).debounce(300).onEach { (sounds, pid) -> ambientRepo.write(AmbientSnapshot(sounds.filter { it.isEnabled }.map { SavedSound(it.id, it.volume, it.organicMotion) }, pid)) }.launchIn(viewModelScope)` |
| [composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt) | `viewModel { MixerViewModel(presetRepo = MixPresetRepository(), ambientRepo = AmbientStateRepository()) }`. (Or hoist a single `JsonStore` and pass to both.) |

`MixerScreen.kt`, `CurrentMixModal.kt`, `SaveMixDialog.kt` need **no changes** — `existingMixNames` continues to derive from `uiState.savedMixes` (now backed by repo flow).

---

## 5. Data Shapes

```kotlin
@Serializable
data class SavedSound(
    val id: String,
    val volume: Float,
    val organicMotion: Boolean,
)

@Serializable
data class MixPreset(
    val id: String,
    val name: String,
    val sounds: List<SavedSound>,
    val createdAt: Long,
)

@Serializable
data class AmbientSnapshot(
    val sounds: List<SavedSound>,
    val loadedPresetId: String? = null,
)
```

`SoundCategory` and `ImageVector` stay out of persistence — sounds are reconstituted from `SoundCatalog` by id.

### JSON examples

`mixer.saved_mixes.v1`
```json
[
  {
    "id": "1714000000000_-871234",
    "name": "Rainy Cafe",
    "sounds": [
      { "id": "rain", "volume": 0.7, "organicMotion": false },
      { "id": "cafe", "volume": 0.4, "organicMotion": true }
    ],
    "createdAt": 1714000000000
  }
]
```

`mixer.ambient_snapshot.v1`
```json
{
  "sounds": [
    { "id": "rain", "volume": 0.65, "organicMotion": false },
    { "id": "wind", "volume": 0.5,  "organicMotion": false }
  ],
  "loadedPresetId": null
}
```

---

## 6. Hydration Flow

```
App() composable created
  └── viewModel { MixerViewModel(MixPresetRepository(), AmbientStateRepository()) }
        ├── MixPresetRepository.<init>
        │     └── store.read("mixer.saved_mixes.v1") → List<MixPreset>  (or [])
        ├── AmbientStateRepository.<init>          (no eager read)
        ├── ambientRepo.read() → AmbientSnapshot?
        ├── MixRepository(catalog, initialSnapshot = snapshot)
        │     ├── if snapshot != null → map onto catalog (drop unknown ids)
        │     └── else → seed() (rain on, wind on, others off)
        ├── _loadedPresetId = snapshot?.loadedPresetId
        │     ?.takeIf { id -> presetRepo.state.value.any { it.id == id } }
        ├── _isPlaying = true                      (always)
        └── orchestrator.start(...)                (begins playback on hydrated state)
```

---

## 7. Persistence Flow

```
User changes volume / toggles sound / loads preset / saves mix
  ↓
MixRepository.update { ... }   (for sound changes)
  or _loadedPresetId.value =   (for preset load)
  or presetRepo.save(...)      (for save — persists immediately, not debounced)
  ↓
combine(repo.state, _loadedPresetId)
  .debounce(300ms)
  .onEach { (sounds, pid) ->
      ambientRepo.write(AmbientSnapshot(activeSavedSounds(sounds), pid))
  }
  .launchIn(viewModelScope)
  ↓
JsonStore.write → Settings[KEY] = JSON
```

`MixPresetRepository.save/delete` calls `persist()` synchronously — list mutations are infrequent enough that debouncing buys nothing.

---

## 8. Edge Cases & Decisions

- **Empty/missing ambient snapshot** → fall back to `MixRepository.seed()` (rain on, wind on).
- **`loadedPresetId` references a deleted/unknown preset** → ignore, hydrate raw sound state, leave `loadedPresetId = null`.
- **Snapshot contains sound ids not in `SoundCatalog`** → silently drop during hydration map.
- **Snapshot missing some catalog sounds** → those sounds hydrate as disabled with default volume.
- **Versioned keys (`v1`)** → on schema break, bump to `v2` and leave `v1` orphaned (no migration framework).
- **Name uniqueness** → `MixPresetRepository.nameExists(name, excludingId)` is the single source of truth. UI continues to receive a `Set<String>` derived from `uiState.savedMixes` (no UI changes).
- **Playback policy** → `isPlaying` is never persisted; constructor default `true` is the contract.
- **Crash before debounce flush** → up to 300ms of state loss is acceptable.
- **Background JSON I/O** → `Settings` is synchronous; payloads are tiny (~KB). Acceptable on caller thread (`viewModelScope` default dispatcher). Revisit if profiling shows jank.

---

## 9. Testing Checklist (manual)

1. **Save mix → kill → reopen** → mix appears in saved list.
2. **Load preset → kill → reopen** → app starts playing the preset's sounds; `loadedPresetId` is set; no dirty badge.
3. **Adjust volume → wait > 300ms → kill → reopen** → new volume restored.
4. **Adjust volume → kill within 300ms** → previous persisted state restored (acceptable loss).
5. **Fresh install** → defaults appear (rain on, wind on, playing).
6. **Delete preset that was loaded → kill → reopen** *(once delete UI exists; pre-test via debugger)* → falls back to raw sounds, `loadedPresetId = null`.
7. **Save mix, change volume of one sound, kill → reopen** → preset list intact AND ambient reflects post-edit volume; `loadedPresetId` retained (dirty after launch is OK since `isDirtyFromPreset` is in-memory and resets to false — acceptable for v1).
8. **Toggle every sound off → kill → reopen** → opens with empty active mix, still `isPlaying = true` (silent).

---

## 10. Out of Scope

- Koin DI migration.
- UI for editing / renaming / deleting saved mixes.
- iCloud / cross-device sync.
- Android-specific DataStore implementation.
- Schema migration framework (`v1 → v2` codepath).
- Persistence of `isPlaying`, `selectedCategory`, `sessionMasterVolume`, organic-motion offsets.
- Persisting `isDirtyFromPreset` across launches.

---

## 11. Implementation Sequence (one commit each)

1. **Add serialization plugin + json dependency.** Update [gradle/libs.versions.toml](gradle/libs.versions.toml) and [composeApp/build.gradle.kts](composeApp/build.gradle.kts). Annotate `MixPreset` and `SavedSound` with `@Serializable`. Verify both `:composeApp:compileDebugKotlin` and `:composeApp:compileKotlinIosSimulatorArm64` pass.
2. **Create `JsonStore` + `MixPresetRepository`.** New files under `feature/mixer/data/`. No UI wiring yet. Compile.
3. **Create `AmbientStateRepository` + `AmbientSnapshot`.** New file under `feature/mixer/data/`. Compile.
4. **Wire `MixRepository`** to accept optional `initialSnapshot: AmbientSnapshot?` and hydrate from it (drop unknown ids, default missing sounds to disabled). Compile.
5. **Wire `MixerViewModel`:**
   - Add `presetRepo` and `ambientRepo` constructor params (default-args).
   - Replace internal `_savedMixes` with `presetRepo.state` in the `combine` block.
   - Validate hydrated `loadedPresetId` against `presetRepo.state.value`.
   - Set `_isPlaying = true` (already is).
   - Add debounced `combine(repo.state, _loadedPresetId).debounce(300).onEach { ambientRepo.write(...) }.launchIn(viewModelScope)` in `init`.
   - Replace `saveCurrentMix` to call `presetRepo.save(preset)`.
   - Compile.
6. **Wire `App.kt`** to construct repos and pass them to `MixerViewModel`. Compile.
7. **Manual verification** per §9 on iOS simulator (and Android if convenient).
