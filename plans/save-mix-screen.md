# SaveMixScreen — Implementation Plan

> **Type:** New feature (UI + in-memory ViewModel state). No persistence layer.
> **Status:** Ready for Developer agent execution.
> **Related:** [plans/current-mix-modal-redesign.md](current-mix-modal-redesign.md), [plans/mixer-refactor.md](mixer-refactor.md), [docs/mixer-screen-refactor.md](../docs/mixer-screen-refactor.md)
> **Design system source of truth:** Serena memory `design_system`

---

## Overview

Build the **Save Mix** screen — a full-screen overlay reached from the bookmark/save action in `CurrentMixModal`. The user enters a name for the current sound mix, taps Save, sees a 2.5s confirmation animation, then returns to `CurrentMixModal` (which stays open).

This slot already exists today as `MixerIntent.OpenSaveDialog`, currently a no-op in [MixerViewModel.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt). That intent is being **removed** and replaced with a screen-local visibility flag in `MixerScreenContent` plus a real `SaveCurrentMix` intent that mutates ViewModel state.

**What we are NOT building here:** persistence (no SQLDelight, no settings storage), no "Saved Mixes" list rendering on the Mixer screen, no preset loading, no preset deletion, no rename. Saved mixes live in a `MutableStateFlow<List<MixPreset>>` inside `MixerViewModel` for this milestone only — they evaporate on app kill.

---

## Architectural Decisions

1. **In-memory only.** `savedMixes` is a `MutableStateFlow<List<MixPreset>>` on the ViewModel. No repository, no persistence interface yet — keeps the surface small until product validates the flow.
2. **Already-saved bookmark tap is a no-op.** When the loaded preset is unmodified, the SaveMixButton dims to 0.35 alpha, label flips to "Saved", `onClick` does nothing. No second screen, no confirmation toast.
3. **Duplicate names rejected inline.** Case-insensitive trim compare against `savedMixes[*].name`. Save button disabled while colliding; inline error text "A mix with this name already exists." rendered in 11sp Light, `MaterialTheme.colorScheme.error`.
4. **Dismiss after save closes only `SaveMixScreen`.** `CurrentMixModal` stays open underneath. (Spec originally said "close both"; user overrode.)
5. **`SavedSound` carries only `id`, `volume`, `organicMotion`.** Never read `liveVolume` — that is engine-derived live data, not a saveable field.
6. **`MixPreset` lives in `domain/`,** not `model/` — matches the post-refactor convention where all mixer domain types are under `feature/mixer/domain/` (see [SoundState.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/SoundState.kt)).
7. **Visibility hosted by `MixerScreenContent`.** Local `var showSaveMixScreen by remember { mutableStateOf(false) }`, sibling to existing `showCurrentMixModal`. `CurrentMixModal` gains an `onOpenSaveMix: () -> Unit` lambda param. **`MixerIntent.OpenSaveDialog` is removed** from the contract and from `MixerViewModel.onIntent`.
8. **Dirty tracking on the ViewModel.** Two new flows: `_loadedPresetId: MutableStateFlow<String?>` and `_isDirtyFromPreset: MutableStateFlow<Boolean>`. A private `markDirtyIfNeeded()` flips `_isDirtyFromPreset` true when `_loadedPresetId.value != null && !_isDirtyFromPreset.value`. Called from `ToggleSound`, `AdjustVolume`, `ToggleOrganicMotion`, `RemoveFromMix`, `ToggleGlobalOrganicMotion`. **Not** from `TogglePlayback` or `SelectCategory` — those don't mutate the mix.
9. **`savedMixes` flow folded into `combine(...)`.** The current 4-arg combine (`repo.state`, `_isPlaying`, `_selectedCategory`, `orchestrator.offsets`) becomes a 7-arg combine including the three new flows. UI-state surfaces `savedMixes`, `loadedPresetId`, `isDirtyFromPreset`.
10. **`SaveMixButton` extracted to its own file.** Move from [DoneButton.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/DoneButton.kt) into `ui/modal/SaveMixButton.kt`. New params: `isDirty: Boolean`, `alreadySaved: Boolean`. When `alreadySaved` → label "Saved", alpha 0.35f, no-op click. The shared `ModalActionButton` private composable stays in `DoneButton.kt` and is widened to `internal` so `SaveMixButton.kt` can reuse it.
11. **`SoundChip` is a new shared composable** at `ui/modal/SoundChip.kt` — small icon + name pill used by the SaveMixScreen "SAVING" preview row. Inline `Color(0xFF191E25)` background (already a documented design-system exception per `current-mix-modal-redesign.md`); no need to wrap in `LuxuryCardSurface` since this chip has no top highlight.

---

## File Map

| Action | Path | Notes |
|---|---|---|
| CREATE | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixScreen.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/SaveMixScreen.kt) | Top-level screen composable; sibling to `CurrentMixModal`. Stateless content + thin stateful wrapper. |
| CREATE | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixPreset.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/domain/MixPreset.kt) | `MixPreset` + `SavedSound` data classes. `@Immutable`. |
| CREATE | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/SaveMixButton.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/SaveMixButton.kt) | Extracted from `DoneButton.kt`; gains `isDirty`/`alreadySaved` params. |
| CREATE | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/SoundChip.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/SoundChip.kt) | Reusable mini pill (icon + name). |
| MODIFY | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt) | Add `savedMixes`, `loadedPresetId`, `isDirtyFromPreset` to `MixerUiState`. Add `SaveCurrentMix(name)` intent. **Remove `OpenSaveDialog`.** |
| MODIFY | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) | Add three flows; expand `combine(...)` to 7 args; add `markDirtyIfNeeded()`; add `SaveCurrentMix` handler; remove `OpenSaveDialog` branch. |
| MODIFY | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) | Add `onOpenSaveMix: () -> Unit`, `isDirtyFromPreset: Boolean`, `alreadySaved: Boolean` params. Replace `MixerIntent.OpenSaveDialog` call with `onOpenSaveMix()`. Pass dirty flags into `SaveMixButton`. |
| MODIFY | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) | Add `var showSaveMixScreen`. Compute `alreadySaved`. Pass new params into `CurrentMixModal`. Render `SaveMixScreen(...)` as sibling overlay. |
| MODIFY | [composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/DoneButton.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/DoneButton.kt) | Remove `SaveMixButton`. Widen private `ModalActionButton` to `internal` so `SaveMixButton.kt` can call it. |

---

## Contract Changes

### `MixerUiState` (final shape)

```kotlin
data class MixerUiState(
    val isPlaying: Boolean = true,
    val sounds: List<SoundState> = emptyList(),
    val selectedCategory: SoundCategory = SoundCategory.ALL,
    val activeSoundsSummary: String = "",
    val activeSoundCount: Int = 0,
    val savedMixes: List<MixPreset> = emptyList(),
    val loadedPresetId: String? = null,
    val isDirtyFromPreset: Boolean = false,
)
```

### `MixerIntent` (final shape)

```kotlin
sealed interface MixerIntent {
    data object TogglePlayback : MixerIntent
    data class ToggleSound(val soundId: String) : MixerIntent
    data class AdjustVolume(val soundId: String, val volume: Float) : MixerIntent
    data class ToggleOrganicMotion(val soundId: String) : MixerIntent
    data class RemoveFromMix(val soundId: String) : MixerIntent
    data object ToggleGlobalOrganicMotion : MixerIntent
    data class SaveCurrentMix(val name: String) : MixerIntent
    data class SelectCategory(val category: SoundCategory) : MixerIntent
}
```

### `MixPreset` + `SavedSound`

```kotlin
package com.focusritual.app.feature.mixer.domain

import androidx.compose.runtime.Immutable

@Immutable
data class MixPreset(
    val id: String,                  // generated client-side; e.g. timestamp+random
    val name: String,                // trimmed; never empty
    val sounds: List<SavedSound>,
    val createdAt: Long,             // epoch millis (kotlinx.datetime Clock.System.now().toEpochMilliseconds())
)

@Immutable
data class SavedSound(
    val id: String,
    val volume: Float,
    val organicMotion: Boolean,
)
```

> **Note:** `liveVolume` is intentionally excluded — it is engine-derived runtime data, not part of the preset.

---

## ViewModel Changes

### New private flows

```kotlin
private val _savedMixes = MutableStateFlow<List<MixPreset>>(emptyList())
private val _loadedPresetId = MutableStateFlow<String?>(null)
private val _isDirtyFromPreset = MutableStateFlow(false)
```

### Expanded `combine(...)`

The current 4-arg combine becomes 7-arg. Signature reminder: `kotlinx.coroutines.flow.combine` has overloads up to 5 typed args; for 6+ use the vararg `combine(vararg flows, transform)` form **or** the typed builder `kotlinx.coroutines.flow.combine` with explicit list. Recommended: nest two combines, or use the vararg + manual cast — match whatever convention the project's ViewModel layer prefers (no nested combine exists today, so the vararg form is acceptable). Pseudocode:

```kotlin
val uiState: StateFlow<MixerUiState> = combine(
    repo.state,
    _isPlaying,
    _selectedCategory,
    orchestrator.offsets,
    _savedMixes,
    _loadedPresetId,
    _isDirtyFromPreset,
) { values ->
    @Suppress("UNCHECKED_CAST")
    val sounds            = values[0] as List<SoundState>
    val isPlaying         = values[1] as Boolean
    val category          = values[2] as SoundCategory
    val offsets           = values[3] as Map<String, Float>
    val savedMixes        = values[4] as List<MixPreset>
    val loadedPresetId    = values[5] as String?
    val isDirtyFromPreset = values[6] as Boolean

    val withLive = if (offsets.isEmpty()) sounds
                   else sounds.map { it.copy(liveVolume = offsets[it.id]) }
    val summary = summarizeActiveMix(withLive, isPlaying)
    MixerUiState(
        isPlaying = isPlaying,
        sounds = withLive,
        selectedCategory = category,
        activeSoundsSummary = summary.activeSoundsSummary,
        activeSoundCount = summary.activeSoundCount,
        savedMixes = savedMixes,
        loadedPresetId = loadedPresetId,
        isDirtyFromPreset = isDirtyFromPreset,
    )
}.stateIn(viewModelScope, SharingStarted.Eagerly, MixerUiState())
```

### `markDirtyIfNeeded()`

```kotlin
private fun markDirtyIfNeeded() {
    if (_loadedPresetId.value != null && !_isDirtyFromPreset.value) {
        _isDirtyFromPreset.value = true
    }
}
```

Call sites (top of these `onIntent` branches, before delegating to use case):

- `is MixerIntent.ToggleSound`
- `is MixerIntent.AdjustVolume`
- `is MixerIntent.ToggleOrganicMotion`
- `is MixerIntent.RemoveFromMix`
- `MixerIntent.ToggleGlobalOrganicMotion`

**Do NOT call** from `TogglePlayback` or `SelectCategory` — these are not mix mutations.

### `SaveCurrentMix` handler

```kotlin
is MixerIntent.SaveCurrentMix -> {
    val activeSounds = repo.state.value.filter { it.isEnabled }
    val preset = MixPreset(
        id = generatePresetId(),
        name = intent.name.trim(),
        sounds = activeSounds.map { SavedSound(it.id, it.volume, it.organicMotion) },
        createdAt = Clock.System.now().toEpochMilliseconds(),
    )
    _savedMixes.update { it + preset }
    _loadedPresetId.value = preset.id
    _isDirtyFromPreset.value = false
}
```

`generatePresetId()` — private helper, e.g. `"${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt()}"`. Multiplatform-safe via `kotlinx.datetime.Clock` and `kotlin.random.Random`.

### Remove `OpenSaveDialog`

Delete the `MixerIntent.OpenSaveDialog -> Unit` branch in `onIntent`. Remove the `OpenSaveDialog` variant from `MixerContract.kt`. The Developer agent must compile after each step — `CurrentMixModal.kt` still references it, so update `CurrentMixModal.kt` in the same edit pass.

---

## SaveMixScreen Composable Spec

Public API:

```kotlin
@Composable
fun SaveMixScreen(
    isVisible: Boolean,
    activeSounds: List<SoundState>,
    existingMixNames: List<String>,        // from uiState.savedMixes.map { it.name }
    onSave: (name: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Internal state (in stateful wrapper):

```kotlin
var name by rememberSaveable { mutableStateOf("") }
var showConfirmation by remember { mutableStateOf(false) }
val focusRequester = remember { FocusRequester() }
```

### Container & enter/exit animation

`AnimatedVisibility(visible = isVisible, …)` — match `CurrentMixModal`'s pattern (separate scrim + content layers). Use these tweens (carry over from spec):

- Scrim: `fadeIn(tween(300, easing = FocusRitualEasing.Atmospheric))` / `fadeOut(tween(250, easing = FocusRitualEasing.CinematicIn))`
- Content: `fadeIn(tween(350, easing = FocusRitualEasing.Ritual)) + slideInVertically(initialOffsetY = { it }, tween(400, easing = FocusRitualEasing.Ritual))`; exit symmetric with `FocusRitualEasing.CinematicIn`
- Background = `MaterialTheme.colorScheme.surface`, with `statusBarsPadding()` + `navigationBarsPadding()`

> If `FocusRitualEasing.Ritual` or `Atmospheric` is not yet defined in [theme/Motion.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt), reuse `DeepEaseOut` and document the substitution in the implementation PR.

### Header

- Top row: `CloseButton(onClick = onDismiss)` (left), centered title stack (right side empty).
- Title: "SAVE THIS MIX" — 10sp Normal, `letterSpacing = 0.14.em`, `onSurface@0.28`.
- Subtitle: "Name your atmosphere" — 11sp Light, `onSurface@0.46`.
- Padding/sizing: match `ModalHeader` (24dp horizontal inset).

### "SAVING" label + SoundChip preview row

- Label: "SAVING" — 9sp Normal, `letterSpacing = 0.18.em`, `onSurface@0.32`. 24dp top inset.
- `FlowRow` (Compose 1.4+ foundation `androidx.compose.foundation.layout.FlowRow`) centered; 8dp horizontal/vertical spacing; one `SoundChip(sound)` per `activeSound`.
- `SoundChip` shape: `RoundedCornerShape(50%)`; bg `Color(0xFF191E25)`; 0.5dp border `outlineVariant@0.20`; height ~30dp; padding 12dp horizontal; icon 12dp + 6dp gap + name 11sp Light onSurface@0.62.

### Mix name input

- `BasicTextField` (never `TextField` — see design rules). `value = name`, `onValueChange = { if (it.length <= 40) name = it }`.
- 40-char hard cap.
- Single-line; `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Words)`.
- `keyboardActions = KeyboardActions(onDone = { if (canSave) onSave(name.trim()) })`.
- `FocusRequester` + `LaunchedEffect(Unit) { focusRequester.requestFocus() }` so keyboard auto-opens on screen entry.
- Decoration box: outer `Box` with rounded shape (24dp), bg `Color(0xFF191E25)`, animated border (0.5dp `primary@0.55` when focused, `outlineVariant@0.20` when idle) — use `animateColorAsState(tween(250))`.
- Placeholder: "e.g. Forest Cabin" — 14sp Light, `onSurface@0.32`, shown only when `name.isEmpty()`.
- Text style: 15sp Normal, `onSurface@0.85`.
- Cursor brush: `SolidColor(MaterialTheme.colorScheme.primary)`.

#### Inline duplicate-name error

Compute:

```kotlin
val trimmed = name.trim()
val duplicate = trimmed.isNotEmpty() &&
    existingMixNames.any { it.equals(trimmed, ignoreCase = true) }
val canSave = trimmed.isNotEmpty() && !duplicate
```

If `duplicate` is true, render under the input (8dp top spacing, 24dp horizontal inset):

```kotlin
Text(
    text = "A mix with this name already exists.",
    fontSize = 11.sp,
    fontWeight = FontWeight.Light,
    color = MaterialTheme.colorScheme.error,
)
```

### Save button states

Single button at bottom; backgrounds animate via `animateColorAsState(tween(300))`:

| State | Condition | bg | text | textColor | onClick |
|---|---|---|---|---|---|
| Empty / Invalid | `!canSave` | `primary@0.10` | "Save Mix" | `primary@0.40` | no-op |
| Active | `canSave` | `primary@0.85` | "Save Mix" | `onPrimary` | `{ onSave(trimmed); showConfirmation = true }` |

Reuse `ModalActionButton` (now `internal`) from [DoneButton.kt](../composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/ui/modal/DoneButton.kt). Note: `ModalActionButton` currently bakes `RoundedCornerShape(24.dp)` and 48dp height — match this spec.

### Cancel button

Below Save (7dp gap). Reuse `ModalActionButton` with the same dark fill as today's `SaveMixButton` (`surfaceContainer@0.50` bg, `outlineVariant@0.32` border). Label "Cancel", 13sp Light, `onSurface@0.62`. `onClick = onDismiss`.

### State 3 — confirmation overlay

Triggered when `showConfirmation = true` (after `onSave` fires). Overlays the form with a fade swap (Crossfade between form and confirmation).

- Concentric rings: 2–3 expanding circles, animated via `animateFloatAsState` on a target `1f → 1.4f` scale + alpha `0.6f → 0f`, staggered by 200ms.
- Center: filled circle (`primary@0.18` bg, 0.5dp `primary@0.40` border), 64dp, with `Icons.Filled.Check` 28dp, `tint = primary`.
- Caption "Mix saved" — 13sp Light, `onSurface@0.72`, 16dp top spacing.
- Done button below (reuse `ModalActionButton`, primary tint identical to current `DoneButton`).
- Auto-dismiss:

```kotlin
LaunchedEffect(showConfirmation) {
    if (showConfirmation) {
        delay(2500)
        onDismiss()
    }
}
```

Manual Done button also calls `onDismiss()` immediately.

---

## CurrentMixModal Wiring

New public signature:

```kotlin
@Composable
fun CurrentMixModal(
    isVisible: Boolean,
    activeSounds: List<SoundState>,
    isPlaying: Boolean,
    anyOrganicOn: Boolean,
    organicMotionSummary: String = "",
    allSoundsOrganic: Boolean = false,
    isDirtyFromPreset: Boolean,         // NEW
    alreadySaved: Boolean,              // NEW
    onIntent: (MixerIntent) -> Unit,
    onOpenSaveMix: () -> Unit,          // NEW
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- Thread `isDirtyFromPreset` and `alreadySaved` down through `ModalContent` → bottom button column.
- Replace the existing `SaveMixButton(onClick = { onIntent(MixerIntent.OpenSaveDialog) })` with:

```kotlin
SaveMixButton(
    isDirty = isDirtyFromPreset,
    alreadySaved = alreadySaved,
    onClick = onOpenSaveMix,
)
```

- `SaveMixButton` internally handles the `alreadySaved` no-op (don't gate at the call site).

---

## MixerScreen Wiring

In `MixerScreenContent`, alongside the existing `var showCurrentMixModal`:

```kotlin
var showSaveMixScreen by remember { mutableStateOf(false) }

val alreadySaved = remember(uiState.loadedPresetId, uiState.isDirtyFromPreset) {
    uiState.loadedPresetId != null && !uiState.isDirtyFromPreset
}
val existingMixNames = remember(uiState.savedMixes) { uiState.savedMixes.map { it.name } }
```

Update the `CurrentMixModal(...)` call:

```kotlin
CurrentMixModal(
    isVisible = showCurrentMixModal,
    activeSounds = activeSounds,
    isPlaying = uiState.isPlaying,
    anyOrganicOn = anyOrganicOn,
    organicMotionSummary = activeOrganicMotionSummary,
    allSoundsOrganic = allSoundsOrganic,
    isDirtyFromPreset = uiState.isDirtyFromPreset,
    alreadySaved = alreadySaved,
    onIntent = onIntent,
    onOpenSaveMix = { showSaveMixScreen = true },
    onDismiss = { showCurrentMixModal = false },
)
```

Add the sibling overlay **after** `CurrentMixModal` so it stacks on top:

```kotlin
SaveMixScreen(
    isVisible = showSaveMixScreen,
    activeSounds = activeSounds,
    existingMixNames = existingMixNames,
    onSave = { name ->
        onIntent(MixerIntent.SaveCurrentMix(name))
        // SaveMixScreen will trigger onDismiss after its own 2500ms confirmation delay
    },
    onDismiss = { showSaveMixScreen = false },
)
```

---

## Design Rules Checklist

Strict — every file in this plan must comply. From `design_system` memory + this project's convention:

- [ ] No `Color.White` / `Color.Black` anywhere.
- [ ] No raw `Color(0xFF...)` except the documented `Color(0xFF191E25)` (sound card bg) carried over from `current-mix-modal-redesign.md`.
- [ ] All borders **0.5dp**.
- [ ] No ripple — every `clickable` uses `interactionSource = remember { MutableInteractionSource() }, indication = null`. Press feedback via `scale(0.97f)` (`graphicsLayer`).
- [ ] `FontWeight.Light` (W300) and `FontWeight.Normal` (W400) only — no Medium, no Bold.
- [ ] `BasicTextField` only — no Material `TextField` / `OutlinedTextField`.
- [ ] **Never read `liveVolume`** when constructing `SavedSound` — use `volume` only.
- [ ] All tokens from `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*` (or explicit sp/em as in this project's micro-typography), `Spacing.*`, `FocusRitualEasing.*`.
- [ ] **Padding gotcha:** never combine `Modifier.padding(horizontal = …, bottom = …)`. Use explicit `start = …, end = …, bottom = …` (per [/memories/repo/build-validation.md](/memories/repo/build-validation.md)).
- [ ] Auto-dismiss / `LaunchedEffect` patterns mirror `CurrentMixModal`'s shape.

---

## Build & Verification

Per [/memories/repo/build-validation.md](/memories/repo/build-validation.md):

```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

Both must pass clean. No new unit tests required — this milestone is in-memory UI plumbing; the persistence layer (which would warrant tests) is explicitly out of scope. A manual smoke check on iOS sim covering: open mix → tap save → enter name → confirm → return to modal → reopen → button shows "Saved" dimmed.

Watch out for:
- The 7-arg `combine` syntax — vararg form requires the `Array<Any?>` cast pattern shown above; double-check Kotlin compiler accepts it under this project's KMP version.
- `kotlinx.datetime.Clock` requires the `kotlinx-datetime` dependency. If absent from `commonMain` deps, fall back to a platform-supplied epoch (or accept the small `expect/actual` hop). Check [gradle/libs.versions.toml](../gradle/libs.versions.toml) before introducing.
- After removing `OpenSaveDialog`, ensure no other call sites reference it (grep `OpenSaveDialog` — should be only the modal's old SaveMixButton wiring).

---

## Out of Scope / Future Work

- **Persistence** — SQLDelight or `multiplatform-settings` to survive app kill. Will require a `SavedMixRepository` interface + expect/actual storage.
- **Saved Mixes list rendering** — a horizontally-scrolling row of preset chips on the Mixer screen above the category pills.
- **Loading a preset** — `LoadPreset(presetId)` intent that calls `repo.replaceState(...)`, sets `_loadedPresetId.value = id`, sets `_isDirtyFromPreset.value = false`.
- **Preset deletion** — long-press or swipe on saved chip; `DeletePreset(id)` intent.
- **Rename flow** — likely a small inline edit on a saved-mix chip; reuses `SaveMixScreen`'s name validation.
- **Empty-preset save guard** — currently we permit saving with whatever sounds are active; if `activeSounds.isEmpty()` we should probably not even open `SaveMixScreen`. (`SaveMixButton` is already inside `CurrentMixModal`, which auto-dismisses on empty mix, so this is naturally guarded — verify in the smoke test.)

---

## Open Questions

None. User has resolved all ambiguity (see Architectural Decisions §1–§11). Proceed to implementation.
