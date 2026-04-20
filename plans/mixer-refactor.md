# Mixer Feature Refactor Plan

> **Status:** Approved by user, ready for sequential execution.
> **Owner:** Solo developer; phases will be implemented one per Developer-agent prompt.
> **Strictness:** Pure refactor — NO behaviour or visual change, with **two intentional, user-approved exceptions**:
> 1. **Phase 3** — adds `indication = null` to 5 clickables in [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) (small visible polish).
> 2. **Phase 4** — moves side-effects (`organicEngine.enable/disable/updateBase`) out of `MutableStateFlow.update {}` lambdas (deliberate behaviour fix — `update` may re-execute).

---

## Goals

- **SOLID:** introduce `SoundCatalog`, `MixRepository`, use cases, and `MixAudioOrchestrator` so the ViewModel becomes a thin MVI dispatcher.
- **Testability:** pure mappers, pure use cases, audio behind an `AudioCommand` boundary that can be exercised with a fake `AudioPlayer`.
- **Performance:** stable parameters (`@Immutable`), narrower parameters into composables, hoisted per-item lambdas, fewer recompositions.
- **Readability:** split the 594-LOC [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) into focused files; remove the layer-violating `core/audio` → `feature/mixer/model` import.
- **Separation of concerns:** UI ⟂ orchestration ⟂ domain ⟂ audio playback, each layer with a single reason to change.

---

## Current State Summary

File inventory (LOC) — mixer feature + collaborators:

| File | LOC |
|---|---|
| [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) | 594 |
| [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) | 346 |
| [MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) | 209 |
| [MixerContract.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerContract.kt) | 23 |
| [SoundState.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/model/SoundState.kt) | 39 |
| [SoundMixer.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt) | 50 |
| [SoundResources.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundResources.kt) | 19 |
| [OrganicMotionEngine.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/OrganicMotionEngine.kt) | 160 |
| [SoundTile.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/SoundTile.kt) | 224 |
| [VolumeSlider.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/VolumeSlider.kt) | 135 |
| [PlayButton.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/PlayButton.kt) | 219 |

Key smells (from the prior investigation report):

- **Layer-direction violation:** [SoundMixer.kt#L3](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt#L3) imports `feature.mixer.model.SoundState` — `core/audio` depends on a feature.
- **God ViewModel:** [MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) owns audio wiring, organic-motion engine, derived UI fields, resource I/O, and intent reduction (~209 LOC).
- **Side-effects inside `update {}`:** [MixerViewModel.kt#L98-L173](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L98-L173) call `organicEngine.enable/disable/updateBase` from inside `_uiState.update { … }` lambdas. `update` is allowed to re-invoke the lambda → duplicated side-effects.
- **OCP violation — sound icon dispatch:** `SoundIcon` enum + `SoundIcon.toImageVector()` `when` in [SoundTile.kt#L48-L58](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/SoundTile.kt#L48-L58) means adding a sound requires touching three places.
- **Duplicated tokens:** `GlowColor` + `OrganicEasing` are defined in both [MixerScreen.kt#L74-L75](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L74-L75) and [PlayButton.kt#L34-L35](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/PlayButton.kt#L34-L35).
- **Design-system violations:**
  - Hex literal `Color(0xFF0c0e11)` 3× in `ImmersiveBackground` at [MixerScreen.kt#L437-L440](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L437-L440), and 1× in [CurrentMixModal.kt#L79](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L79).
  - `Color.White` / `Color.Black` in HeroSessionButton at [MixerScreen.kt#L226-L247](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L226-L247).
  - 5 clickables in [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) missing `indication = null` — at [L141](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L141), [L184](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L184), [L260](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L260), [L274](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L274), [L308](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L308).
- **Recomposition over-broadness:** `CurrentMixPanel` and `CurrentMixModal` take the entire `MixerUiState`; per-item lambdas inside the `LazyColumn` items in [MixerScreen.kt#L168-L181](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L168-L181) are recreated every recomposition.
- **Resource I/O in VM:** `loadSoundResources()` at [MixerViewModel.kt#L78-L90](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L78-L90) couples the VM to the resource loader.

---

## Target Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│  UI (Compose, commonMain)                                           │
│  feature/mixer/MixerScreen.kt        — stateful entry                │
│  feature/mixer/ui/*                  — split sections (Phase 1)      │
│  feature/mixer/modal/*               — modal pieces (Phase 1)        │
│      │                                                              │
│      │  uiState: StateFlow<MixerUiState>     onIntent: (Intent)→Unit │
│      ▼                                                              │
├─────────────────────────────────────────────────────────────────────┤
│  ViewModel (commonMain)                                             │
│  feature/mixer/MixerViewModel.kt   ~80 LOC: collect repo, dispatch   │
│      │                                                              │
│      │  intent dispatch  /  setSessionMasterVolume                   │
│      ▼                                                              │
├─────────────────────────────────────────────────────────────────────┤
│  Domain (commonMain) — Phase 4                                      │
│  feature/mixer/domain/                                              │
│    ├── MixRepository          — single source of truth for SoundState│
│    ├── SoundCatalog(+Impl)    — id, name, icon, category, resPath    │
│    ├── usecase/*              — Toggle/Adjust/Remove/Select…         │
│    ├── audio/MixAudioOrchestrator — owns SoundMixer + OrganicMotion  │
│    └── MixerMappers           — pure (Phase 2)                       │
│      │                                                              │
│      │  AudioCommand(id, volume, enabled)                            │
│      ▼                                                              │
├─────────────────────────────────────────────────────────────────────┤
│  core/audio (commonMain) — pure data boundary (Phase 5)             │
│  AudioCommand.kt              — pure data; NO feature import         │
│  SoundMixer.syncState(commands)                                      │
│  AudioPlayer (expect/actual)                                         │
│  AudioPlayerFactory           — for testability                      │
│  OrganicMotionEngine          — unchanged behaviour                  │
└─────────────────────────────────────────────────────────────────────┘
```

What stays where:
- `core/audio` ends Phase 5 with **zero** imports from `feature/mixer`.
- `core/designsystem/theme/Motion.kt` becomes the home for shared `GlowColor` + `OrganicEasing` (Phase 3).
- `feature/mixer/model/SoundState.kt` is the UI-state type (gains `@Immutable`); the `SoundIcon` enum is dropped in Phase 4.
- Resource paths + icons live in `SoundCatalogImpl` after Phase 4.

---

## Guardrails (apply to every phase)

- **MVI contracts preserved:** `MixerUiState` field set, `MixerIntent` cases — must remain backward-compatible unless a phase explicitly says otherwise. None of phases 1–5 changes the public contract shape.
- **LiveActivityEffect contract preserved:** `isPlaying`, `activeSoundsSummary`, `activeSoundCount` must continue to be derivable from `MixerUiState` with the same semantics as today (see [MixerViewModel.kt#L194-L202](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L194-L202)).
- **`App.kt` API preserved:** `mixerViewModel.uiState`, `mixerViewModel.onIntent(...)`, and `mixerViewModel::setSessionMasterVolume` (see [App.kt#L39-L164](composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt#L39-L164)) must keep working. Constructors may add **optional** parameters only (default values).
- **Build validation** after every phase per [/memories/repo/build-validation.md](/memories/repo/build-validation.md):
  - `./gradlew :composeApp:assembleDebug`
  - `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
- **Design system** rules from the `design_system` memory: only `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `Spacing.*`, `FocusRitualEasing.*` in feature code; no raw hex; no `Color.White`/`Color.Black`; no ripple.
- **Compose stability:** use `@Immutable` annotations only — do **not** add `kotlinx.collections.immutable`.
- **Padding rule:** never combine `Modifier.padding(horizontal = …, bottom = …)` — use explicit `start`/`end` (per build-validation memory).

---

## Phase 1 — UI Component Extraction

### Goal
Cut the 594-LOC [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) and 346-LOC [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) into focused, single-purpose files. Pure cut/paste — **no signature changes**, **no logic changes**.

### Scope (files to create)

Create new package `feature/mixer/ui/` with:

| New file | Source range moved |
|---|---|
| `feature/mixer/ui/HeroSessionButton.kt` | [MixerScreen.kt#L184-L294](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L184-L294) (`HeroSessionButton`) |
| `feature/mixer/ui/CurrentMixPanel.kt` | [MixerScreen.kt#L296-L417](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L296-L417) (`CurrentMixPanel`) |
| `feature/mixer/ui/ImmersiveBackground.kt` | [MixerScreen.kt#L419-L443](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L419-L443) (`ImmersiveBackground`) |
| `feature/mixer/ui/CategoryPillRow.kt` | [MixerScreen.kt#L445-L532](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L445-L532) (`CategoryPillRow` + private `CategoryPill`) |
| `feature/mixer/ui/SectionHeader.kt` | [MixerScreen.kt#L534-L594](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L534-L594) (`SectionHeader`) |

Create new package `feature/mixer/modal/` with:

| New file | Source range moved |
|---|---|
| `feature/mixer/modal/ModalHeader.kt` | [CurrentMixModal.kt#L126-L188](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L126-L188) |
| `feature/mixer/modal/GlobalOrganicMotionRow.kt` | [CurrentMixModal.kt#L190-L226](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L190-L226) |
| `feature/mixer/modal/ActiveSoundRow.kt` | [CurrentMixModal.kt#L228-L319](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L228-L319) |
| `feature/mixer/modal/DoneButton.kt` | [CurrentMixModal.kt#L321-L345](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L321-L345) |

Files modified in place:
- [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) — keeps `MixerScreen`, `MixerScreenContent`, top-level imports; deletes the moved private composables; adds imports for the new modules. `private val GlowColor` / `OrganicEasing` move with `HeroSessionButton`.
- [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) — keeps the public `CurrentMixModal` shell; deletes the four extracted private composables.

### Out of scope
- Any signature change. Do not introduce mappers, narrower parameters, or new tokens.
- Fixing ripple/hex/Color.White issues — those belong to Phase 3.
- Touching `MixerViewModel`, `SoundMixer`, or any non-UI file.

### Behaviour change?
**None.** Pure cut/paste reorganisation.

### Acceptance criteria
- [ ] All 9 new files exist with the correct package declarations (`com.focusritual.app.feature.mixer.ui` / `…modal`).
- [ ] Every extracted composable is `internal` or `@Composable internal fun` (was `private` before; widen to `internal` so the parent file can call it). Visibility widening is the only allowed signature change.
- [ ] [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) drops below ~200 LOC; [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) drops below ~130 LOC.
- [ ] No new imports from `core/audio` or `feature/mixer/domain` (Phase 4 territory).
- [ ] App launches and Mixer screen renders identically (manual smoke check on iOS sim).

### Build validation commands
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Risk notes
- Visibility change `private → internal` is the cleanest move; alternative (top-level `@Composable fun` left `private` per file) also works since each file scopes its own `private`. Either is acceptable provided the parent file compiles.
- Watch out for the `private val GlowColor` / `private val OrganicEasing` at [MixerScreen.kt#L74-L75](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L74-L75): they are referenced only by `HeroSessionButton`; move them with it. Phase 3 will dedupe with `PlayButton.kt`.

---

## Phase 2 — Pure Mappers + Stable Parameters

### Goal
Eliminate over-broad recomposition by:
1. Annotating `SoundState` with `@Immutable`.
2. Introducing pure mappers that produce **narrow, stable** DTOs for the panel and modal.
3. Hoisting per-item callbacks out of the `LazyColumn` items lambda.

### Scope

**New files:**

- `feature/mixer/domain/MixerMappers.kt` — pure, top-level functions:
  - `fun summarizeActiveMix(sounds: List<SoundState>): CurrentMixSummary` (replaces the inline `withDerivedFields` summary logic in [MixerViewModel.kt#L194-L202](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L194-L202)).
  - `fun groupActiveSounds(sounds: List<SoundState>, selectedCategory: SoundCategory): GroupedSounds` (replaces the inline grouping in [MixerViewModel.kt#L39-L48](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L39-L48)).
- `feature/mixer/domain/MixerDtos.kt`:
  ```kotlin
  @Immutable data class CurrentMixSummary(
      val isPlaying: Boolean,
      val activeSoundCount: Int,
      val activeSoundsSummary: String,
  )
  @Immutable data class GroupedSounds(
      val byCategory: Map<SoundCategory, List<SoundState>>,
  )
  ```

**Files modified:**

- [SoundState.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/model/SoundState.kt) — add `@Immutable` to `data class SoundState`.
- [MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) — call `summarizeActiveMix(...)` from `withDerivedFields`; call `groupActiveSounds(...)` from the `filteredSounds` flow. **No behaviour change** — same output, extracted location.
- `feature/mixer/ui/CurrentMixPanel.kt` (created in Phase 1) — change parameter from `uiState: MixerUiState` to `summary: CurrentMixSummary` + `onTogglePlayback: () -> Unit` + `onPanelTap: () -> Unit`. `MixerScreenContent` builds the summary (or reads from the VM-derived state).
- `feature/mixer/CurrentMixModal.kt` — change parameter from `uiState: MixerUiState` to a narrower bundle `(activeSounds: List<SoundState>, isPlaying: Boolean, anyOrganicOn: Boolean)`. Re-derive these via mappers in the parent.
- [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) — inside the `items(...)` block at [L168-L181](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L168-L181), hoist per-sound callbacks with `remember(sound.id, onIntent)`:
  ```kotlin
  val onToggle = remember(sound.id, onIntent) { { onIntent(MixerIntent.ToggleSound(sound.id)) } }
  ```
  Same treatment for `AdjustVolume`, `ToggleOrganicMotion`. Apply equivalent hoisting in `ActiveSoundRow` items in the modal.

### Out of scope
- Use cases, repository, or audio-layer changes (Phase 4/5).
- Removing `SoundIcon` enum (Phase 4).

### Behaviour change?
**None.** Pure-data extractions and stability hints. Mappers must produce byte-identical results to the inline code they replace.

### Acceptance criteria
- [ ] `MixerMappers.kt` and `MixerDtos.kt` exist; mappers are top-level pure `fun`.
- [ ] `SoundState` carries `@Immutable`.
- [ ] `CurrentMixPanel` no longer references `MixerUiState`.
- [ ] `CurrentMixModal` no longer references the full `MixerUiState`.
- [ ] In the LazyColumn items in MixerScreen and the LazyColumn items in CurrentMixModal, per-item lambdas use `remember(sound.id, onIntent) { … }`.
- [ ] `activeSoundsSummary` and `activeSoundCount` produced by `summarizeActiveMix` match the existing `withDerivedFields` output for the same input (logic is copied verbatim).

### Build validation commands
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Risk notes
- Any mismatch in `summarizeActiveMix` semantics breaks the LiveActivity contract. **Copy the logic exactly** from [MixerViewModel.kt#L194-L202](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L194-L202) — including the `count <= 2` branch and the `+${count - 2}` suffix.
- `GroupedSounds.byCategory` must keep `LinkedHashMap` order matching [MixerViewModel.kt#L41-L47](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L41-L47).

---

## Phase 3 — Design-System Compliance

### Goal
Bring the mixer feature in line with the `design_system` memory's hard rules. Includes the **one approved visible polish**.

### Scope

**Modified — [Color.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Color.kt):**
- Reuse the existing `Surface = Color(0xFF0c0e11)` token (already declared at [Color.kt#L5](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Color.kt#L5)). No new token needed — the literal already matches `Surface`.
- *If* a darker shade is required for the modal's `0xFF0c0e11.copy(alpha = 0.98f)` use case (reads near-black), keep using `MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)`. **Decision: do NOT add `surfaceDeepest` — `colorScheme.surface` resolves to `Surface = 0xFF0c0e11` already.**

**Modified — [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) (and the file `ImmersiveBackground.kt` it became after Phase 1):**
- Replace the 3 `Color(0xFF0c0e11)` hex literals at [L437-L440](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L437-L440) with `MaterialTheme.colorScheme.surface`.
- In `HeroSessionButton.kt` (post-Phase-1), replace `Color.White.copy(...)` at [L226-L237](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L226-L237) with `MaterialTheme.colorScheme.onSurface.copy(...)` and `Color.Black.copy(...)` at [L246-L247](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L246-L247) with `MaterialTheme.colorScheme.scrim.copy(...)` (we map scrim to `0xFF0c0e11`; if visual diff > 0, switch to `surfaceContainerLowest`).

**Modified — [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt):**
- Replace `Color(0xFF0c0e11).copy(alpha = 0.98f)` at [L79](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L79) with `MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)`.
- Add `indication = null, interactionSource = remember { MutableInteractionSource() }` to all 5 ripple-violating clickables:
  - [L141](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L141) — Close icon
  - [L184](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L184) — Play/Pause icon
  - [L260](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L260) — Organic motion icon
  - [L274](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L274) — Remove icon
  - [L308](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L308) — Done button

**Modified — [Motion.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt):**
- Add public tokens (single source of truth):
  ```kotlin
  val GlowColor = Color(0xFFB7C8DB)              // matches Primary
  val OrganicEasing = CubicBezierEasing(0.3f, 0.0f, 0.15f, 1.0f)
  ```
- Note: `0xFFB7C8DB` is the existing `Primary` token at [Color.kt#L9](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Color.kt#L9). Decision: **expose as `GlowColor` in Motion.kt** (preserving the semantic name) but reference `Primary` internally if equal — actually keep as a literal in `Motion.kt` to avoid coupling motion tokens to colorScheme resolution timing.

**Modified — [PlayButton.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/PlayButton.kt) and `feature/mixer/ui/HeroSessionButton.kt`:**
- Delete the duplicate `private val GlowColor` and `private val OrganicEasing` (was [PlayButton.kt#L34-L35](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/PlayButton.kt#L34-L35) and [MixerScreen.kt#L74-L75](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L74-L75)).
- Import from `core.designsystem.theme.GlowColor` / `OrganicEasing`.

### Out of scope
- Restructuring the audio layer or VM (Phase 4/5).
- The other ripple violations elsewhere in the codebase (only the 5 in `CurrentMixModal` are in scope).

### Behaviour change?
**Yes — single intentional visible polish:** ripple effect removed from 5 modal clickables (replaced with no indication; press feedback already absent — matches the rest of the app). User-approved per Decision #2.

### Acceptance criteria
- [ ] `grep -RnE "Color\(0xFF[0-9a-fA-F]{6}\)" composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/` returns 0 matches.
- [ ] `grep -RnE "Color\.(White|Black)" composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/` returns 0 matches.
- [ ] All 5 listed clickables in `CurrentMixModal.kt` carry `indication = null`.
- [ ] `GlowColor` and `OrganicEasing` exist exactly once each in [Motion.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt) and nowhere else.

### Build validation commands
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Risk notes
- `MaterialTheme.colorScheme.scrim` may not be defined on this project's `Theme.kt`. If undefined, fall back to `surfaceContainerLowest` (which is `0xFF000000`) for the `Color.Black` replacement in HeroSessionButton — visually a 0xFF0c0e11 vs 0xFF000000 difference of 12 levels, imperceptible inside an inner-shadow gradient. Verify in [Theme.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Theme.kt) before choosing.
- Ripple removal is the **only** allowed visible change. Do not "while you're at it" change shapes, alphas, or paddings.

---

## Phase 4 — Domain Layer Introduction

### Goal
Shrink `MixerViewModel` to ~80 LOC by extracting (a) the catalog of sounds, (b) the repository of sound state, (c) one use case per intent, and (d) the audio orchestrator that owns `SoundMixer` and `OrganicMotionEngine`. Fold `SoundIcon` data into the catalog.

### Scope

**New files (under `feature/mixer/domain/`):**

| File | Responsibility |
|---|---|
| `domain/SoundCatalog.kt` | Interface + `data class SoundDefinition(id, name, category, icon: ImageVector, resourcePath: String?)`. Provides `fun all(): List<SoundDefinition>`. |
| `domain/SoundCatalogImpl.kt` | Hard-coded list — replaces `defaultSounds()` from [SoundState.kt#L26-L38](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/model/SoundState.kt#L26-L38) AND `SoundResources.soundFiles` from [SoundResources.kt#L4-L14](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundResources.kt#L4-L14). Each `SoundDefinition` carries its `ImageVector` directly (drops `SoundIcon` enum). Also responsible for `loadAudioBytes(definition)` via `Res.readBytes(...)`. |
| `domain/MixRepository.kt` | Owns `MutableStateFlow<List<SoundState>>`. Methods: `state: StateFlow<List<SoundState>>`, `update(transform: (List<SoundState>) -> List<SoundState>)`. |
| `domain/usecase/TogglePlaybackUseCase.kt` | `class …(private val isPlaying: MutableStateFlow<Boolean>)` — `operator fun invoke()`. |
| `domain/usecase/ToggleSoundUseCase.kt` | `(repo, orchestrator).invoke(soundId)`. |
| `domain/usecase/AdjustVolumeUseCase.kt` | `(repo, orchestrator).invoke(soundId, volume)`. |
| `domain/usecase/ToggleOrganicMotionUseCase.kt` | `(repo, orchestrator).invoke(soundId)`. |
| `domain/usecase/RemoveFromMixUseCase.kt` | `(repo, orchestrator).invoke(soundId)`. |
| `domain/usecase/ToggleGlobalOrganicMotionUseCase.kt` | `(repo, orchestrator).invoke()`. |
| `domain/usecase/SelectCategoryUseCase.kt` | `(selectedCategoryFlow).invoke(category)`. |
| `domain/audio/MixAudioOrchestrator.kt` | Owns `SoundMixer` + `OrganicMotionEngine`. Exposes `offsets: StateFlow<Map<String,Float>>`, `start(scope, sounds: StateFlow<List<SoundState>>, isPlaying: StateFlow<Boolean>, sessionMasterVolume: StateFlow<Float?>)` — replaces the triple-`combine` in [MixerViewModel.kt#L52-L72](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L52-L72). Side-effects (`organicEngine.enable/disable/updateBase`) move here, **outside** any `update {}` lambda. |

**Modified files:**

- [SoundState.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/model/SoundState.kt) — drop `SoundIcon` enum and `defaultSounds()`. Keep the `SoundState` data class (still UI-state). Add field `val icon: ImageVector` (was `SoundIcon`); seed values come from the catalog.
- [SoundTile.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/SoundTile.kt) — delete `SoundIcon.toImageVector()` extension at [L48-L58](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/SoundTile.kt#L48-L58); read `state.icon` directly.
- `feature/mixer/modal/ActiveSoundRow.kt` (post-Phase-1) — drop `import …toImageVector`; read `sound.icon` directly.
- [MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) — shrink to ~80 LOC:
  ```kotlin
  class MixerViewModel(
      private val repo: MixRepository = MixRepository(SoundCatalogImpl()),
      private val orchestrator: MixAudioOrchestrator = MixAudioOrchestrator(SoundCatalogImpl()),
      private val toggleSound: ToggleSoundUseCase = ToggleSoundUseCase(repo, orchestrator),
      // … one default per use case
  ) : ViewModel() { … }
  ```
  - `init` block calls `orchestrator.start(viewModelScope, repo.state, isPlaying, sessionMasterVolume)`.
  - `uiState` = `combine(repo.state, isPlaying, selectedCategory, orchestrator.offsets) { … }` producing `MixerUiState` via `summarizeActiveMix` + applying live-volume overlay.
  - `onIntent` is a 7-line `when` dispatching to use cases.
  - `setSessionMasterVolume(volume: Float?)` retained verbatim (writes to the internal `MutableStateFlow<Float?>`).

### Out of scope
- Koin / actual DI wiring (deferred to Phase 6).
- Changing `SoundMixer` signature (Phase 5).
- Adding tests (Phase 5 covers tests).

### Behaviour change?
**Two intentional fixes** (user-approved per Decisions #3 and #4):
1. **Side-effects out of `update {}`:** `organicEngine.enable/disable/updateBase` calls — currently nested inside `_uiState.update { … sound.copy(...) … }` lambdas at [MixerViewModel.kt#L98-L173](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L98-L173) — are moved into the orchestrator, invoked **once per intent** outside any state lambda. This is a deliberate correctness fix: `MutableStateFlow.update`'s lambda may re-execute under contention.
2. **`SoundIcon` enum dropped:** icon `ImageVector` is now data on `SoundDefinition`. Visually identical (same `Icons.Filled.*` references), but adds OCP.

Otherwise no visible/audible change.

### Acceptance criteria
- [ ] `MixerViewModel.kt` ≤ ~100 LOC (target ~80).
- [ ] `MixerViewModel` constructor parameters all have default values (no DI framework yet) — `App.kt`'s `viewModel { MixerViewModel() }` continues to compile.
- [ ] `setSessionMasterVolume(volume: Float?)` signature unchanged.
- [ ] `MixerUiState` shape unchanged (`isPlaying`, `sounds`, `selectedCategory`, `activeSoundsSummary`, `activeSoundCount`).
- [ ] `MixerIntent` cases unchanged.
- [ ] `SoundIcon` enum removed; no `toImageVector()` extension remains.
- [ ] `SoundResources` deleted (logic absorbed into `SoundCatalogImpl`); `core/audio` no longer references `feature/mixer`.
  - Note: the layer-direction hard fix (`SoundMixer` import) is finalised in Phase 5.
- [ ] Grep proves `_uiState.update { ... organicEngine ... }` returns 0 matches.

### Build validation commands
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Risk notes
- **Live-volume overlay:** the current `uiState` flow at [MixerViewModel.kt#L26-L36](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L26-L36) overlays `liveVolume` from `organicEngine.offsets`. The new `combine` in the VM must preserve this exact mapping or `VolumeSlider`'s organic-motion animation breaks.
- **Initial `withDerivedFields()` call** at [MixerViewModel.kt#L50](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L50) seeds `activeSoundsSummary`/`activeSoundCount` for the initial state. Ensure `summarizeActiveMix` runs on the seed value too.
- **`ImageVector` in `@Immutable` data class:** `ImageVector` is stable in Compose runtime; safe to hold in `SoundState`.
- **Audio fade ordering:** the orchestrator must emit `syncState` after the repo emits, with `sessionMasterVolume` change reflected promptly — the existing 3-way `combine` semantics must be preserved.

---

## Phase 5 — Audio Layer Inversion + Tests

### Goal
1. Sever `core/audio`'s import of `feature/mixer/model/SoundState` by introducing a pure `AudioCommand` boundary.
2. Make `SoundMixer` testable with a fake `AudioPlayer`.
3. Land the unit-test suite that the prior phases enabled.

### Scope

**New files:**

- `core/audio/AudioCommand.kt`:
  ```kotlin
  data class AudioCommand(
      val id: String,
      val volume: Float,   // 0f..1f — already master-scaled
      val enabled: Boolean,
  )
  ```
- `core/audio/AudioPlayerFactory.kt`:
  ```kotlin
  fun interface AudioPlayerFactory { fun create(): AudioPlayer }
  object DefaultAudioPlayerFactory : AudioPlayerFactory { override fun create() = AudioPlayer() }
  ```
- Tests (under `composeApp/src/commonTest/kotlin/com/focusritual/app/...`):
  - `feature/mixer/domain/MixerMappersTest.kt` — covers `summarizeActiveMix` (0/1/2/3+ active) and `groupActiveSounds` (ALL vs specific category, ordering preserved).
  - `feature/mixer/domain/usecase/ToggleSoundUseCaseTest.kt`, `AdjustVolumeUseCaseTest.kt`, `ToggleOrganicMotionUseCaseTest.kt`, `RemoveFromMixUseCaseTest.kt`, `ToggleGlobalOrganicMotionUseCaseTest.kt`, `SelectCategoryUseCaseTest.kt`, `TogglePlaybackUseCaseTest.kt` — verify state transitions on a fake `MixRepository` + recording fake orchestrator.
  - `core/audio/SoundMixerTest.kt` — using a `FakeAudioPlayer` (records `play`/`stop`/`setVolume`/`release` + tracks `isPlaying`); injected via `AudioPlayerFactory`.

**Modified files:**

- [SoundMixer.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt):
  - Constructor: `class SoundMixer(private val factory: AudioPlayerFactory = DefaultAudioPlayerFactory)`.
  - Signature: `fun syncState(commands: List<AudioCommand>)` — `isPlaying` and `masterVolume` are folded into `AudioCommand.volume` and `AudioCommand.enabled` by the caller (orchestrator).
  - Remove `import com.focusritual.app.feature.mixer.model.SoundState` at [SoundMixer.kt#L3](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt#L3).
- `domain/audio/MixAudioOrchestrator.kt` (created in Phase 4) — adapt: the existing 3-way `combine` already produces effective volume per sound; map that into `List<AudioCommand>` before calling `soundMixer.syncState(commands)`.
- [SoundResources.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundResources.kt) — deleted in Phase 4; confirm nothing references it.

### Out of scope
- Restructuring `OrganicMotionEngine` (works fine as-is; it has no `SoundState` dependency).
- Adding instrumentation tests; only `commonTest` JVM-runnable tests.

### Behaviour change?
**None.** The `AudioCommand` derivation matches the existing in-flight computation in [MixerViewModel.kt#L60-L67](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L60-L67) and the `effectiveVolume` math in [SoundMixer.kt#L15](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt#L15). `enabled` is computed as `(isPlaying || sessionVolume > 0.01f) && sound.isEnabled`.

### Acceptance criteria
- [ ] `grep -Rn "feature/mixer" composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/` returns 0 matches.
- [ ] `SoundMixer.syncState` takes `List<AudioCommand>` only.
- [ ] All listed unit tests exist and pass: `./gradlew :composeApp:allTests` (or `:composeApp:jvmTest`/`:composeApp:iosSimulatorArm64Test` per project setup).
- [ ] `App.kt` and the iOS LiveActivity bridge unaffected (no changes needed in either).
- [ ] `MixerUiState`, `MixerIntent`, and `setSessionMasterVolume` still compile against the same callers.

### Build validation commands
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
./gradlew :composeApp:allTests
```

### Risk notes
- **`enabled` semantics:** today, when `sessionMasterVolume != null`, `isPlaying` is overridden to `sessionVolume > 0.01f` (see [MixerViewModel.kt#L65](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt#L65)). Mirror this exactly in the `AudioCommand` derivation.
- **`AudioPlayer` is `expect/actual`:** `DefaultAudioPlayerFactory.create()` only works on platform targets; `commonTest` uses the fake. Make sure tests live in `commonTest` and don't accidentally pull `DefaultAudioPlayerFactory`.
- **Test timing:** any flow-based use-case test should use `runTest` + `Turbine` *only if Turbine is already in the project* — otherwise fall back to `flow.first()` with `runTest`. Check [libs.versions.toml](gradle/libs.versions.toml) before adding deps.

---

## Deferred — Phase 6 (post-Koin)

Out of scope for this plan; recorded so it isn't lost:
- Replace default-value constructors with Koin module wiring.
- Move `MixerViewModel` instantiation in [App.kt#L39](composeApp/src/commonMain/kotlin/com/focusritual/app/App.kt#L39) to `koinViewModel()`.
- Bind `SoundCatalog` and `MixRepository` as singletons; use cases as factories.
- Bind `AudioPlayerFactory` per platform if richer audio configuration is needed.

---

## Open Questions / Decisions Log

| # | Decision | Rationale |
|---|---|---|
| 1 | **5 phases** as proposed; Phase 6 deferred. | Matches solo-dev cadence; Koin not yet in project. |
| 2 | Ripple-violation fix in `CurrentMixModal` bundled into Phase 3. | Touches the same 5 lines being audited for DS compliance — packaging avoids a return trip. Polish is small and consistent with the rest of the app. |
| 3 | Side-effects out of `MutableStateFlow.update {}` — fixed in Phase 4, called out as deliberate behaviour fix. | `update`'s contract permits re-execution; the current code can double-fire `organicEngine.enable`. Behaviour fix is invisible in steady state but eliminates a latent bug. |
| 4 | `SoundIcon` enum + `toImageVector()` `when` dropped in Phase 4; icon data folded into `SoundCatalog`. | OCP: adding a sound currently touches 3 files. After Phase 4, only `SoundCatalogImpl` changes. |
| 5 | Compose stability uses `@Immutable` annotations only — **no `kotlinx.collections.immutable`**. | Keeps dependency footprint small; `@Immutable` is sufficient since `Map`/`List` literals from mappers don't mutate. |

---

## File-Move Map (appendix)

Legend: **C** = create, **M** = modify, **D** = delete, **MV** = move (split out of an existing file).

| Phase | Action | Path | Source |
|---|---|---|---|
| 1 | MV | `feature/mixer/ui/HeroSessionButton.kt` | from [MixerScreen.kt#L184-L294](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L184-L294) |
| 1 | MV | `feature/mixer/ui/CurrentMixPanel.kt` | from [MixerScreen.kt#L296-L417](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L296-L417) |
| 1 | MV | `feature/mixer/ui/ImmersiveBackground.kt` | from [MixerScreen.kt#L419-L443](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L419-L443) |
| 1 | MV | `feature/mixer/ui/CategoryPillRow.kt` | from [MixerScreen.kt#L445-L532](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L445-L532) |
| 1 | MV | `feature/mixer/ui/SectionHeader.kt` | from [MixerScreen.kt#L534-L594](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt#L534-L594) |
| 1 | MV | `feature/mixer/modal/ModalHeader.kt` | from [CurrentMixModal.kt#L126-L188](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L126-L188) |
| 1 | MV | `feature/mixer/modal/GlobalOrganicMotionRow.kt` | from [CurrentMixModal.kt#L190-L226](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L190-L226) |
| 1 | MV | `feature/mixer/modal/ActiveSoundRow.kt` | from [CurrentMixModal.kt#L228-L319](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L228-L319) |
| 1 | MV | `feature/mixer/modal/DoneButton.kt` | from [CurrentMixModal.kt#L321-L345](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt#L321-L345) |
| 1 | M | [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) | shrink to ~200 LOC |
| 1 | M | [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) | shrink to ~130 LOC |
| 2 | C | `feature/mixer/domain/MixerMappers.kt` | new |
| 2 | C | `feature/mixer/domain/MixerDtos.kt` | new |
| 2 | M | [SoundState.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/model/SoundState.kt) | add `@Immutable` |
| 2 | M | [MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) | call mappers |
| 2 | M | `feature/mixer/ui/CurrentMixPanel.kt` | narrower params |
| 2 | M | [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) | narrower params |
| 2 | M | [MixerScreen.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerScreen.kt) | hoist per-item lambdas |
| 3 | M | [Motion.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/theme/Motion.kt) | add `GlowColor`, `OrganicEasing` |
| 3 | M | `feature/mixer/ui/HeroSessionButton.kt` | drop local tokens; theme references |
| 3 | M | `feature/mixer/ui/ImmersiveBackground.kt` | drop hex literals |
| 3 | M | [PlayButton.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/PlayButton.kt) | drop local tokens |
| 3 | M | [CurrentMixModal.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/CurrentMixModal.kt) | hex → token; ripple fix on 5 clickables |
| 4 | C | `feature/mixer/domain/SoundCatalog.kt` | new interface + DTO |
| 4 | C | `feature/mixer/domain/SoundCatalogImpl.kt` | absorbs `defaultSounds()` + `SoundResources` |
| 4 | C | `feature/mixer/domain/MixRepository.kt` | new |
| 4 | C | `feature/mixer/domain/usecase/TogglePlaybackUseCase.kt` | new |
| 4 | C | `feature/mixer/domain/usecase/ToggleSoundUseCase.kt` | new |
| 4 | C | `feature/mixer/domain/usecase/AdjustVolumeUseCase.kt` | new |
| 4 | C | `feature/mixer/domain/usecase/ToggleOrganicMotionUseCase.kt` | new |
| 4 | C | `feature/mixer/domain/usecase/RemoveFromMixUseCase.kt` | new |
| 4 | C | `feature/mixer/domain/usecase/ToggleGlobalOrganicMotionUseCase.kt` | new |
| 4 | C | `feature/mixer/domain/usecase/SelectCategoryUseCase.kt` | new |
| 4 | C | `feature/mixer/domain/audio/MixAudioOrchestrator.kt` | new |
| 4 | M | [MixerViewModel.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/MixerViewModel.kt) | shrink to ~80 LOC |
| 4 | M | [SoundState.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/feature/mixer/model/SoundState.kt) | drop `SoundIcon`, drop `defaultSounds()`; `icon: ImageVector` |
| 4 | M | [SoundTile.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/designsystem/component/SoundTile.kt) | drop `SoundIcon.toImageVector()` |
| 4 | M | `feature/mixer/modal/ActiveSoundRow.kt` | use `sound.icon` directly |
| 4 | D | [SoundResources.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundResources.kt) | absorbed by `SoundCatalogImpl` |
| 5 | C | `core/audio/AudioCommand.kt` | new pure data |
| 5 | C | `core/audio/AudioPlayerFactory.kt` | new |
| 5 | M | [SoundMixer.kt](composeApp/src/commonMain/kotlin/com/focusritual/app/core/audio/SoundMixer.kt) | accept `List<AudioCommand>`; remove `feature/mixer` import |
| 5 | M | `feature/mixer/domain/audio/MixAudioOrchestrator.kt` | derive `AudioCommand`s |
| 5 | C | `commonTest/.../feature/mixer/domain/MixerMappersTest.kt` | tests |
| 5 | C | `commonTest/.../feature/mixer/domain/usecase/*Test.kt` (×7) | tests |
| 5 | C | `commonTest/.../core/audio/SoundMixerTest.kt` | tests w/ fake `AudioPlayer` |
