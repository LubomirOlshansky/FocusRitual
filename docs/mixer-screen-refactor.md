# MixerScreen Refactor — Immersive Ambient Home Screen

## Overview

Refactor the existing `MixerScreen` into an immersive ambient home screen.
No redesign from scratch — modify existing composables, ViewModel, and UiState only.

**Design intent:** User enters the app and is immediately inside a calm ambient atmosphere.
One large hero button for starting a session. One floating bottom panel for mix playback.
Sound cards for mixing. Nothing else.

---

## Current Architecture (verified from code)

### Files involved

| File | Path | Role |
|------|------|------|
| `MixerScreen.kt` | `feature/mixer/` | `MixerScreen` → `MixerScreenContent` → `ImmersiveBackground` |
| `MixerContract.kt` | `feature/mixer/` | `MixerUiState` + `MixerIntent` |
| `MixerViewModel.kt` | `feature/mixer/` | `_uiState`, `SoundMixer`, `onIntent()`, `loadSoundResources()` |
| `SoundState.kt` | `feature/mixer/model/` | `SoundState` data class + `defaultSounds()` + `SoundIcon` enum |
| `PlayButton.kt` | `designsystem/component/` | 96dp glass circle, play/pause icon, breathing rings + glow |
| `SoundTile.kt` | `designsystem/component/` | Sound toggle + volume slider per sound |
| `App.kt` | root package | Navigation: `Crossfade` between `Mixer` / `FocusSession` / `ActiveSession` |

### Current MixerUiState

```kotlin
data class MixerUiState(
    val isPlaying: Boolean = false,
    val sceneName: String = "Midnight Rain",       // unused — never read
    val sceneSubtitle: String = "AETHER IMMERSION", // read only in hero Text
    val sounds: List<SoundState> = defaultSounds(),
)
```

### Current MixerIntent

```kotlin
sealed interface MixerIntent {
    data object TogglePlayback : MixerIntent       // ← reuse for floating panel
    data class ToggleSound(val soundId: String) : MixerIntent
    data class AdjustVolume(val soundId: String, val volume: Float) : MixerIntent
}
```

### Current defaultSounds() — 2 sounds already enabled

```kotlin
SoundState(id = "rain",  name = "Rain",  isEnabled = true, volume = 0.7f)
SoundState(id = "wind",  name = "Wind",  isEnabled = true, volume = 0.5f)
// + 7 more sounds (thunder, forest, stream, cafe, fireplace, brown_noise, waves) all disabled
```

### Current MixerScreenContent layout

```
Box
├── ImmersiveBackground (background image + vertical gradient overlay)
└── LazyColumn (contentPadding bottom = 32dp)
    ├── item: Box(fillMaxWidth)
    │   ├── Column(centerHorizontally, top = 120dp)
    │   │   ├── PlayButton (96dp circle, play/pause icon)       ← REMOVE
    │   │   ├── Spacer(24dp)
    │   │   ├── Text(sceneSubtitle, labelSmall, 6sp spacing)    ← REMOVE
    │   │   ├── Spacer(16dp)
    │   │   └── "START SESSION" pill box (48dp, pill shape, gradient bg) ← REMOVE
    │   └── About button (top-right, 34dp circle, Info icon)     ← KEEP
    ├── items: SoundTile per sound                                ← KEEP
    └── (no "Current Scene" block exists — requirement §5 is already satisfied)
├── AboutSheet (conditional modal)                                ← KEEP
```

### Current MixerViewModel.init flow

```
init {
    loadSoundResources()              // async: reads bytes for each sound
    combine(_uiState, _sessionMasterVolume) →
        soundMixer.syncState(sounds, isPlaying, masterVolume)
}
```

Audio starts flowing as soon as `isPlaying = true` in state. Resources are loaded async —
the mixer skips sounds not yet loaded, so switching to `isPlaying = true` by default is safe.

---

## Changes

### 1. MixerContract.kt — State changes

**Remove:**
- `sceneName: String` — never read anywhere in the codebase
- `sceneSubtitle: String` — read only in hero area which is being replaced

**Add:**
- `activeSoundsSummary: String` — derived text for floating panel, e.g. `"Rain • Wind • +2"`
- `activeSoundCount: Int` — number of enabled sounds, controls panel visibility

**Change:**
- `isPlaying` default: `false` → `true` (ambient auto-starts on screen open)

**No new intents needed.** `TogglePlayback` is reused for the floating panel play/pause.

```kotlin
data class MixerUiState(
    val isPlaying: Boolean = true,                   // CHANGED: auto-start
    val sounds: List<SoundState> = defaultSounds(),
    val activeSoundsSummary: String = "",             // NEW
    val activeSoundCount: Int = 0,                    // NEW
)
```

---

### 2. MixerViewModel.kt — Logic changes

#### 2a. Auto-start playback

No code change in ViewModel needed — the default `isPlaying = true` in `MixerUiState` flows into
the existing `combine(_uiState, _sessionMasterVolume)` collector, which calls
`soundMixer.syncState(sounds, isPlaying = true, ...)`.

`defaultSounds()` already has Rain + Wind enabled, so the user hears ambient immediately.

#### 2b. Compute derived fields

Add a private extension and call it in every `_uiState.update` block:

```kotlin
private fun MixerUiState.withDerivedFields(): MixerUiState {
    val enabled = sounds.filter { it.isEnabled }
    val count = enabled.size
    val summary = when {
        count == 0 -> ""
        count <= 2 -> enabled.joinToString(" • ") { it.name }
        else -> enabled.take(2).joinToString(" • ") { it.name } + " • +${count - 2}"
    }
    return copy(activeSoundsSummary = summary, activeSoundCount = count)
}
```

**Call sites** — wrap every `_uiState.update` result:

1. `TogglePlayback`: `it.copy(isPlaying = !it.isPlaying).withDerivedFields()`
2. `ToggleSound`: `state.copy(sounds = ...).withDerivedFields()`
3. `AdjustVolume`: `state.copy(sounds = ...).withDerivedFields()`

Also add initial computation in `init` after `_uiState` creation:
```kotlin
_uiState.update { it.withDerivedFields() }
```

This ensures the initial state (Rain + Wind enabled) already has
`activeSoundsSummary = "Rain • Wind"` and `activeSoundCount = 2`.

---

### 3. MixerScreen.kt — UI refactor (main change)

Three operations inside `MixerScreenContent`:

#### 3a. REMOVE from hero area

Remove the entire `Column` content inside the first `item` block:
- ❌ `PlayButton(isPlaying, onClick = TogglePlayback)` — 96dp play/pause circle
- ❌ `Spacer(24dp)` + `Text(uiState.sceneSubtitle)` — "AETHER IMMERSION" label
- ❌ `Spacer(16dp)` + `Box(pill shape)` with "START SESSION" — gradient pill button
- ❌ Associated `interactionSource`, `isPressed`, `scale`, `primary`/`primaryContainer` variables

**Keep untouched:**
- ✅ About button `Box` (top-right, `Alignment.TopEnd`)
- ✅ `showAboutSheet` state + `AboutSheet(onDismiss)` conditional

#### 3b. ADD `HeroSessionButton` — private composable

Replace the removed hero content with one large circular glass button.

| Property | Value | Notes |
|----------|-------|-------|
| Size | 150dp diameter | Visually dominant, larger than old 96dp |
| Shape | `CircleShape` | |
| Background | Glass: `surfaceBright.copy(alpha = 0.6f)` idle / `0.8f` when playing | Match PlayButton's pattern |
| Border | `outlineVariant.copy(alpha = 0.12f)`, 1dp | Ghost border per design system |
| Text | "START SESSION" | `labelSmall`, `letterSpacing = 2.sp`, `onSurface.copy(alpha = 0.85f)` |
| Tap | `onStartSession()` | NOT TogglePlayback |
| Shadow | `shadow(24.dp, CircleShape)` | Ambient shadow per design system |

**Breathing animation (when `isPlaying = true`):**

Reuse the exact animation pattern from `PlayButton.kt`:

```kotlin
val infiniteTransition = rememberInfiniteTransition()
val breath by infiniteTransition.animateFloat(
    initialValue = 0f, targetValue = 1f,
    animationSpec = infiniteRepeatable(
        animation = tween(3500, easing = OrganicEasing),
        repeatMode = RepeatMode.Reverse,
    ),
)
val glowIntensity by animateFloatAsState(
    targetValue = if (isPlaying) 1f else 0f,
    animationSpec = tween(800),
)
val scaleAnim = 1f + breath * 0.03f * glowIntensity   // 3% max scale, subtler than PlayButton's 4%
```

**Glow aura** — one soft radial glow behind the circle (no breathing rings — keep it calmer):
```kotlin
.drawBehind {
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to GlowColor.copy(alpha = 0.15f),
                0.4f to GlowColor.copy(alpha = 0.06f),
                1.0f to Color.Transparent,
            ),
            radius = size.width * 0.7f,
        ),
        alpha = glowIntensity,
    )
}
```

Where `GlowColor` = `Color(0xFFb7c8db)` (primary token, same as PlayButton).

**Note:** `OrganicEasing` and `GlowColor` are currently `private` in `PlayButton.kt`.
Developer should either extract them to a shared file or duplicate the definitions.

**Idle state (not playing):**
- `glowIntensity = 0f` → no glow, no scale pulse
- Background alpha drops to 0.6f (dimmer glass)

#### 3c. ADD `CurrentMixPanel` — floating bottom overlay

Place **inside the outer `Box`** of `MixerScreenContent`, as a sibling after `LazyColumn`
and before `AboutSheet`.

**Positioning:**
```kotlin
Box(
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(horizontal = 24.dp, bottom = 32.dp)
        .fillMaxWidth()
)
```

**Visibility:**
```kotlin
AnimatedVisibility(
    visible = uiState.activeSoundCount > 0,
    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
    exit  = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 },
)
```

Since `defaultSounds()` has 2 enabled sounds, the panel is visible from app start.
Panel hides only if the user manually disables all sounds (edge case, acceptable).
Panel auto-disappears on navigation because `MixerScreenContent` only composes on `AppScreen.Mixer`.

**Layout:**
```
Box (RoundedCornerShape(20dp), glass bg, border, shadow)
└── Row (verticalAlignment = CenterVertically, padding 16dp)
    ├── Column (weight(1f))
    │   ├── "CURRENT MIX" — labelSmall, letterSpacing = 2.sp, onSurface alpha 0.6
    │   └── activeSoundsSummary — bodySmall, onSurface alpha 0.85
    └── Row (spacing 4dp)
        ├── IconButton: Pause/PlayArrow → onIntent(TogglePlayback)
        └── Icon: KeyboardArrowUp (12dp, decorative, onSurfaceVariant alpha 0.4)
```

**Glass styling — must feel distinct from SoundTile:**

| Property | Value | vs SoundTile |
|----------|-------|--------------|
| Background | `surfaceContainerHighest.copy(alpha = 0.90f)` | SoundTile uses `surfaceContainerHigh.copy(alpha = 0.80f)` |
| Border | `outlineVariant.copy(alpha = 0.15f)` | Same ghost border rule |
| Corner | `RoundedCornerShape(20.dp)` | Matches SoundTile |
| Shadow | `shadow(16.dp, shape)` | Floating elevation — SoundTile has none |

The higher surface token + shadow + higher opacity creates clear visual separation.

#### 3d. Adjust LazyColumn bottom padding

Change `contentPadding = PaddingValues(bottom = 32.dp)` → `PaddingValues(bottom = 120.dp)`.

This gives room for: floating panel (~64dp) + bottom margin (32dp) + scroll breathing room (24dp).

#### 3e. Update imports

**Add:**
- `AnimatedVisibility`, `fadeIn`, `fadeOut`, `slideInVertically`, `slideOutVertically`
- `infiniteRepeatable`, `rememberInfiniteTransition`, `RepeatMode`
- `animateColorAsState`
- `Row`
- `Icons.Filled.Pause`, `Icons.Filled.PlayArrow`, `Icons.Filled.KeyboardArrowUp`
- `IconButton`
- `shadow` (from `androidx.compose.ui.draw`)

**Remove:**
- `com.focusritual.app.core.designsystem.component.PlayButton` import
- `CornerRadius`, `Size` (only used in old pill button glow code)

---

### 4. Confirmed: No "Current Scene" block exists

The requirement §5 ("Remove Current Scene block") refers to something that does **not exist**
in the current code. There is no "Current Scene" title, "Rainy Ridge Peak" text, save button,
or master slider anywhere in `MixerScreenContent`. **Already satisfied — no action needed.**

### 5. Files NOT modified

| File | Reason |
|------|--------|
| `App.kt` | Navigation already calls `onStartSession()` → `AppScreen.FocusSession`. No change. |
| `PlayButton.kt` | No longer imported in MixerScreen. Keep file — may be reused in future. |
| `SoundTile.kt` | Explicitly excluded per requirements. |
| `SoundState.kt` | `defaultSounds()` already has Rain + Wind enabled. No change needed. |
| `AboutSheet.kt` | Untouched per requirements. |

---

## Final layout after refactor

```
Box (fillMaxSize)
├── ImmersiveBackground()
├── LazyColumn (fillMaxSize, contentPadding bottom = 120dp)
│   ├── item: Box(fillMaxWidth)
│   │   ├── Column(centerHorizontally, top = 120dp)
│   │   │   └── HeroSessionButton (150dp circle, "START SESSION", breathing glow)
│   │   └── About button (top-right, 34dp, Info icon)            ← unchanged
│   └── items: SoundTile per sound                                ← unchanged
├── CurrentMixPanel (AnimatedVisibility, BottomCenter, floating)
│   └── Row: "CURRENT MIX" + summary | Play/Pause + Chevron
└── AboutSheet (conditional modal)                                ← unchanged
```

---

## Animation summary

| Element | Trigger | Effect | Duration |
|---------|---------|--------|----------|
| HeroSessionButton scale | `isPlaying = true` | `1f + breath * 0.03f * glowIntensity` | 3500ms infinite reverse |
| HeroSessionButton glow | `isPlaying = true` | Radial gradient aura, alpha = glowIntensity | 800ms fade in |
| HeroSessionButton bg alpha | `isPlaying` toggle | 0.6f (idle) ↔ 0.8f (playing) | 300ms tween |
| HeroSessionButton idle | `isPlaying = false` | Static, no pulse, dimmer glass | — |
| CurrentMixPanel enter | `activeSoundCount > 0` | `fadeIn + slideInVertically` | 400ms |
| CurrentMixPanel exit | `activeSoundCount == 0` | `fadeOut + slideOutVertically` | 300ms |

---

## UX flow

1. App opens → `MixerScreen` composes
2. `MixerUiState(isPlaying = true)` → `SoundMixer.syncState()` starts Rain + Wind audio
3. Hero button shows "START SESSION" with gentle breathing glow
4. Floating panel shows "CURRENT MIX" / "Rain • Wind" with pause button
5. User can toggle/mix sounds via SoundTile cards — panel summary updates live
6. User taps panel pause → all ambient audio pauses → hero button goes static
7. User taps panel play → ambient resumes → hero button breathes again
8. User taps hero "START SESSION" → `onStartSession()` → navigates to FocusSession
9. Panel disappears automatically (MixerScreen unmounts in Crossfade)

---

## Risks & notes

1. **Auto-start is safe**: `isPlaying = true` in default state works because `loadSoundResources()` runs async and `SoundMixer.syncState()` skips sounds not yet cached. Audio plays as soon as resources are ready.
2. **Default sounds already enabled**: `defaultSounds()` has Rain (0.7) + Wind (0.5) enabled. Initial `activeSoundsSummary = "Rain • Wind"`, `activeSoundCount = 2`. No code change needed in `SoundState.kt`.
3. **Panel always visible on launch**: 2 sounds enabled by default → panel appears immediately. Only hides if user disables all 9 sounds — acceptable edge case.
4. **Panel disappears on navigation**: `MixerScreenContent` only composes when `currentScreen == AppScreen.Mixer` inside `Crossfade` in `App.kt`. No extra hide logic needed.
5. **Bottom padding 120dp**: Panel height ~64dp + bottom padding 32dp + buffer 24dp = 120dp. May need minor visual tuning.
6. **`PlayButton.kt` kept**: File remains in codebase. Only the import in `MixerScreen.kt` is removed.
7. **`OrganicEasing` + `GlowColor`**: Currently private in `PlayButton.kt`. Developer must extract to shared location or duplicate definitions in `MixerScreen.kt`.
