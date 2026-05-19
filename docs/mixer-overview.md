# Mixer Screen — Implementation Overview

Kotlin Multiplatform / Compose Multiplatform. All UI in `commonMain`. Dark-only design system.

---

## File Map

```
feature/mixer/
  MixerContract.kt             — MVI state/intent types
  MixerViewModel.kt            — business logic, audio orchestration, preset persistence
  MixerScreen.kt               — stateful host + stateless MixerScreenContent composable
  ui/
    HeroSessionButton.kt       — 150dp "START SESSION" primary call-to-action
    CurrentMixPanel.kt         — bottom-pinned bar: active sounds + play/pause + expand chevron
    ImmersiveBackground.kt     — forest photo + vertical gradient scrim
    CategoryPillRow.kt         — horizontal sound category filter
    SectionHeader.kt           — per-category section labels in the sound list
    SoundTile.kt               — per-sound row: toggle, volume slider, organic-motion
    SavedMixesGhostRow.kt      — tappable ghost row that opens PresetsSheet
    PresetsSheet.kt            — saved-mix browser bottom sheet

core/designsystem/component/
  PlayButton.kt                — standalone play/pause button with 3-ring breathing animation
                                 (used in CurrentMixPanel and the session screen)
```

---

## MVI Contract

```kotlin
data class MixerUiState(
    val isPlaying: Boolean = true,
    val sounds: List<SoundState> = emptyList(),
    val selectedCategory: SoundCategory = SoundCategory.ALL,
    val activeSoundsSummary: String = "",
    val activeSoundCount: Int = 0,
    val activeSoundNames: List<String> = emptyList(),
    val savedMixes: List<MixPreset> = emptyList(),
    val loadedPresetId: String? = null,       // non-null = a preset is currently active
    val isDirtyFromPreset: Boolean = false,   // true = user modified a loaded preset
)

sealed interface MixerIntent {
    data object TogglePlayback : MixerIntent
    data class ToggleSound(val soundId: String) : MixerIntent
    data class AdjustVolume(val soundId: String, val volume: Float) : MixerIntent
    data class ToggleOrganicMotion(val soundId: String) : MixerIntent
    data class RemoveFromMix(val soundId: String) : MixerIntent
    data object ToggleGlobalOrganicMotion : MixerIntent
    data class SaveCurrentMix(val name: String) : MixerIntent
    data class LoadMix(val presetId: String) : MixerIntent
    data class DeleteMix(val presetId: String) : MixerIntent
    data class SelectCategory(val category: SoundCategory) : MixerIntent
}
```

---

## ViewModel

Constructor params: `SoundCatalog`, `MixPresetRepository`, `AmbientStateRepository`, `MixRepository`, `MixAudioOrchestrator`, `HapticController`, plus use-case objects.

Key internal state:
- `_isPlaying: MutableStateFlow(true)` — starts playing on launch
- `_selectedCategory: MutableStateFlow(SoundCategory.ALL)`
- `_loadedPresetId: MutableStateFlow<String?>` — hydrated from `AmbientStateRepository` on init (drops ids not present in saved presets)
- `_isDirtyFromPreset: MutableStateFlow(false)`

`uiState` is built from a 7-argument `combine`:
`(sounds, isPlaying, selectedCategory, organicOffsets, savedMixes, loadedPresetId, isDirtyFromPreset)` → `MixerUiState`.

### Intent handling

| Intent | Behavior |
|--------|---------|
| `TogglePlayback` | inverts `_isPlaying`; no haptic |
| `ToggleSound` | haptic `soundTileEnabled()` only when the sound was previously off; marks dirty |
| `AdjustVolume` | marks dirty |
| `ToggleOrganicMotion` | marks dirty |
| `RemoveFromMix` | marks dirty |
| `ToggleGlobalOrganicMotion` | marks dirty |
| `SaveCurrentMix` | saves preset, fires `hapticController.mixSaved()`, sets `_loadedPresetId`, clears dirty |
| `LoadMix` | loads snapshot, fires `hapticController.mixLoaded()`, sets `_loadedPresetId`, clears dirty |
| `DeleteMix` | deletes preset; clears `_loadedPresetId` if it was the active one |
| `SelectCategory` | updates `_selectedCategory` |

### Ambient persistence (debounced)

`combine(sounds, loadedPresetId, isDirtyFromPreset)` debounced 300ms → writes `AmbientSnapshot`:
- Preset loaded + **not** dirty → persist with `loadedPresetId`
- No preset / dirty → persist with `null` (reopens as custom config; no active preset chip)

### `reloadAmbientSnapshot()`
Called by `App.kt` after onboarding completes. Reads `AmbientSnapshot` from disk and hydrates rain + wind volumes into the current mix.

---

## Screen Layout

```
Box (fillMaxSize)
├── ImmersiveBackground()
│     forest photo (ContentScale.Crop, Alignment.TopCenter)
│     verticalGradient scrim:
│       transparent@0.0 → surface@0.6 @0.35 → surface@0.92 @0.55 → surface@1.0
│
├── LazyColumn (contentPadding bottom 140dp)
│   ├── item: Box
│   │   ├── Column (padding top 120dp, CenterHorizontally)
│   │   │   └── HeroSessionButton(isPlaying, onStartSession)
│   │   └── Settings icon button (Alignment.TopEnd, pt 62 pe 24)
│   │       28dp circle, surfaceContainerHighest@0.70, border 0.5dp outlineVariant@0.18
│   │       scale 1→0.97 / icon alpha 0.62→0.44 on press (tween 160ms)
│   ├── item: CategoryPillRow
│   ├── item (if savedMixes not empty): SavedMixesGhostRow → opens PresetsSheet
│   └── per category:
│       SectionHeader (category name + active count)
│       SoundTile × N (padding h 24dp, bottom 7dp each)
│
├── CurrentMixPanel (Alignment.BottomCenter)
│     visible only when activeSoundCount > 0
│     enter: fadeIn(400) + slideInVertically(400)
│     exit:  fadeOut(300) + slideOutVertically(300)
│
├── CurrentMixModal  (overlay, opened by tapping CurrentMixPanel)
├── PresetsSheet     (overlay, opened by SavedMixesGhostRow)
└── SaveMixDialog    (overlay, opened from CurrentMixModal / PresetsSheet)
```

---

## HeroSessionButton — Deep Dive

**File:** `feature/mixer/ui/HeroSessionButton.kt`
**Size:** 150dp circle. Calls `onStartSession` on tap (no ripple — `indication = null`).

### Animated values

| Value | Kind | Spec | Purpose |
|-------|------|------|---------|
| `breath` | InfiniteTransition float 0→1 | 4000ms OrganicEasing Reverse | drives scale pulse |
| `glowIntensity` | `animateFloatAsState` | 0f→1f with `isPlaying`, tween 800ms | gates all glow layers |
| `scaleAnim` | derived | `1f + breath × 0.025f × glowIntensity` | ±2.5% breathing scale when playing, flat when paused |
| `bgAlpha` | `animateFloatAsState` | 0.65f (paused) → 0.85f (playing), tween 500ms | disc brightness |
| `pressScale` | `animateFloatAsState` | 1f → 0.96f on press, tween 150ms | press feedback |
| **final transform** | combined | `scaleAnim × pressScale` via `graphicsLayer` | single matrix on outer Box |

### Layer stack (drawn via `drawBehind`, outer → inner)

**1. Outer soft glow aura**
Radial gradient using `GlowColor`:
- `0.0 → GlowColor@0.20`, `0.3 → GlowColor@0.10`, `0.6 → GlowColor@0.03`, `1.0 → transparent`
- Radius `size.width × 0.9f` — bleeds beyond the button edge
- Alpha multiplied by `glowIntensity` — fully disappears when paused

**2. Inner luminous core**
Radial gradient using `onSurface`:
- Center `onSurface@0.10` → `@0.04`@0.3 → transparent; radius `size.width × 0.35f`
- Alpha `0.6f + glowIntensity × 0.4f` — always present, brightens when playing

**3. Soft inner shadow ring** (concavity / depth)
Radial gradient:
- transparent@0→0.75 → `scrim@0.08`@0.92 → `scrim@0.15`@1.0; radius `size.width × 0.5f`
- No `glowIntensity` gate — always subtle depth cue

**4. Disc background** — `Brush.radialGradient` on `surfaceBright`:
- `0.0 → @bgAlpha`, `0.4 → @bgAlpha×0.88`, `0.8 → @bgAlpha×0.70`, `1.0 → @bgAlpha×0.55`
- Fades toward the edge so the glow layers bleed through

**5. Border** — 0.5dp `outlineVariant@0.14`, `CircleShape`

**6. Label** — `"START SESSION"` (uppercased), `labelMedium`, `FontWeight.SemiBold`, letterSpacing 1.2sp, `onSurface@0.95`

### Behavior summary

- **Playing:** button breathes ±2.5% on a 4s OrganicEasing cycle; glow aura fully visible; disc bright (`bgAlpha 0.85`)
- **Paused:** breathing scale collapses to zero (multiplied by `glowIntensity = 0`); aura fades over 800ms; disc dims (`bgAlpha 0.65`)
- **Press:** snaps to 0.96× in 150ms, multiplied on top of the breathing scale

---

## PlayButton — Standalone Play/Pause

**File:** `core/designsystem/component/PlayButton.kt`
**Core size:** 96dp circle. Used in `CurrentMixPanel` and the session screen.

Three staggered breathing rings drawn as separate `Box` layers around the core button:

| Ring | Size | Speed | Scale range | Alpha range (×glowIntensity) |
|------|------|-------|-------------|------------------------------|
| Ring 3 (outer) | 220dp | 6800ms OrganicEasing Reverse | 0.88→1.00 | 0.08→0.18 |
| Ring 2 (middle) | 175dp | 5200ms OrganicEasing Reverse | 0.90→1.00 | 0.12→0.26 |
| Ring 1 (inner) | 135dp | 4000ms OrganicEasing Reverse | 0.93→1.00 | 0.16→0.34 |

Additionally:
- **Outer glow aura** (165dp): scale `1.0→1.08`, alpha `0.30→0.55 × glowIntensity`, 3500ms
- **Inner halo ring** (120dp): radial gradient `GlowColor@0.08→0.14`, alpha `0.20→0.38 × glowIntensity`
- **Core disc** (96dp): scale `1.0→1.04`, `surfaceBright@0.6→0.8` animated over 300ms
- `shadow(24.dp, CircleShape)` on core disc
- Icon: `PlayArrow` ↔ `Pause` (40dp, `onSurface`)

All ring/aura alphas are multiplied by `glowIntensity` — everything fades to zero when paused (800ms).

---

## CurrentMixPanel

**File:** `feature/mixer/ui/CurrentMixPanel.kt`
Bottom-pinned, full-width pill. Visible only when `activeSoundCount > 0`.

```
RoundedCornerShape(18dp)  shadow(8dp)
surfaceContainerHighest@0.97 (vertical gradient top→bottom)
border 0.5dp outlineVariant@0.12
padding start 20, end 12, top/bottom 12

Row
├── Column (weight 1)
│   ├── "CURRENT MIX"  labelSmall  ls 0.10sp  onSurfaceVariant@0.40
│   └── activeSoundsSummary  bodyMedium  FontWeight.Light  onSurface@0.78
├── AirPlayButton (40dp circle, onSurface@0.08 bg, 16dp icon)
├── Play/Pause button (40dp circle, onSurface@0.08 bg, 20dp icon)  → TogglePlayback
└── KeyboardArrowUp chevron (14dp, onSurfaceVariant@0.25)
```

Tapping anywhere on the panel opens `CurrentMixModal`.

---

## Design constraints

- Dark-only. All colors via `MaterialTheme.colorScheme.*`. No `Color(0xFF…)`, no `Color.White`.
- No ripple anywhere (`indication = null`). Press feedback via `animateFloatAsState` scale.
- Border thickness: 0.5dp everywhere.
- `GlowColor` and `OrganicEasing` are defined in `core/designsystem/theme/`.
- `softBlur` shim not used on the mixer screen (used on onboarding only).
